package playground

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBufferShadow
import org.openrndr.draw.renderTarget
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import playground.iterativeImageWarping.CentricScaleRotateWarper.ang
import playground.iterativeImageWarping.CentricScaleRotateWarper.angOff
import playground.iterativeImageWarping.CentricScaleRotateWarper.scl
import playground.iterativeImageWarping.CentricScaleRotateWarper.sclOff
import playground.iterativeImageWarping.WarpProvider
import playground.iterativeImageWarping.WobbleWarper
import utils.CornerPos.*
import utils.cornerAt
import utils.displayLinesOfText
import kotlin.system.measureTimeMillis

// Globals
val w = 640
val h = 480
val size = w*h

val base_delta = Vector2(-100.0, 0.0)
var delta_fac = 1.0
val delta_fac_off = 0.1
var delta_rot = 0.0
val delta_rot_off = 5.0
var delta_x = base_delta

var max_iter = 5

/**
 * Refs: https://la.disneyresearch.com/wp-content/uploads/Iterative-Image-Warping-Paper.pdf
 *
 * Explanation:
 * We keep an accumulative render target, that blends incoming partial results.
 * We generate a new result from a source image with a specified warping.
 *
 * A warping takes a source pixel x_s to a warped location x_w by offset, named V:x_s -> off.
 * Thus, x_w = x_s + V(x_s) [1]
 *
 * Forward sampling is expensive and leads to discontinuities.
 * We invert the warping by defining a contraction G(x_s) = x_w - V(x_s),
 * rewriting [1] as x_s = G(x_s).
 *
 * For now, assume an x_0 that produces x_1 = G(x_0) and x_2 so on that converges quickly against x* = x_s.
 * Our sampling of x_0 is crucial.
 * As described in the paper, just picking x_0 = x_w (which is a good guess for static parts)
 * is favoring static background over moving overlaps.
 *
 * Some fixed offset ∆_x will do fine, especially for small non-linear warping.
 * I.e. x_0 = x_w + ∆_x
 *
 * We still need to take care of non-convergent iterations.
 * Should we encounter a G(x_s) with eigenvalues only greater 1 in absolute value,
 * then the original pixel x_w needs to be interpolated somehow.
 * As we are in 2D, we only have to deal with a 2x2 matrix A = ((a b)(c d)).
 * The eigenvalues of A (with trace T = a+d and determinant D = ad - bc) are
 * l_1 = T/2 + (T^2/4-D)^1/2 ,
 * l_2 = T/2 - (T^2/4-D)^1/2 .
 * If all eigenvalues yield |l| > 1, interpolate accordingly as no convergence will be reached.
 */
fun main() = application {

    // Convenience funcs
    fun Vector2.isInViewport(): Boolean = (x.toInt() in 0 until w) && (y.toInt() in 0 until h)
    fun Int.toPixelVec() : Vector2 {
        return Vector2((this % w).toDouble(), (this / w).toDouble())
    }

    // Double panel viewport:
    // Left side is original, right side is warp result
    configure {
        this.width=2*w
        this.height=h
    }
    program {

        val warpProvider: WarpProvider = WobbleWarper
        var isShowingGrid = true
        var isCornerDebugging = false

        val rt = renderTarget(w, h) {
            colorBuffer("source")
            colorBuffer("warped")
            colorBuffer("accumulated")
        }
        val sourceShadow = rt.colorBuffer(0).shadow
        val warpedShadow = rt.colorBuffer(1).shadow
        val accumulatedShadow = rt.colorBuffer(2).shadow
        accumulatedShadow.forEachIndex { i ->
            accumulatedShadow[i%w, i/w] = ColorRGBa.BLACK
        }

        fun getSourcePixel(x_w: Vector2, onIteration: (Vector2) -> Unit= {}) : Vector2 {
            // Preprocess contraction
            val warp = warpProvider.getV(x_w, program.seconds, program.deltaTime)
            // Pick x_0 (warp pixel, but with offset)
            var currentX = x_w + base_delta.rotate(delta_rot)*delta_fac
            // Iterate over contraction
            repeat(max_iter) {
                currentX = x_w - warp(currentX) // fix point function
                onIteration(currentX)
            }
            return currentX
        }

        fun getSourcePixelColor(x_w: Vector2) : ColorRGBa {
            // Use source pixel
            val x_s = getSourcePixel(x_w)
            return if (x_s.isInViewport()) sourceShadow[x_s] else ColorRGBa.BLACK
        }

        extend {
            measureTimeMillis{
                // Draw on raw
                val blue = mouse.position.x / w
                sourceShadow.forEachIndex { i_s ->
                    val x = i_s % w
                    val y = i_s / w
                    sourceShadow[x, y] = ColorRGBa(x.toDouble() / w, y.toDouble() / h, blue)
                    if (isShowingGrid) {
                        if (x % 100 == 99 || y % 100 == 99) sourceShadow[x, y] = ColorRGBa.WHITE
                    }
                }
                sourceShadow.upload()

                // Draw on warped
                warpedShadow.forEachIndex { i_w ->
                    val x_w = i_w.toPixelVec()
                    val c = getSourcePixelColor(x_w)
                    warpedShadow[x_w] = c
                }

                // Accumulate
                accumulatedShadow.forEachIndex { i_a ->
                    val c = warpedShadow[i_a%w, i_a/w]
                    accumulatedShadow[i_a%w, i_a/w] = c // * 0.20 + accumulatedShadow[i_a%w, i_a/w] * 0.97
                }

                drawer.image(rt.colorBuffer(0), 0.0, 0.0)
                drawer.image(rt.colorBuffer(2), w.toDouble(), 0.0)


                if (isCornerDebugging) {// Colorful debugging
                    val panelRect = Rectangle(0.0, 0.0, w * 1.0, h * 1.0)
                    val pointsOfInterest = listOf(UP_LEFT, UP_RIGHT, DOWN_RIGHT, DOWN_LEFT)
                        .map { panelRect.cornerAt(it) }

                    pointsOfInterest.forEachIndexed { i, p ->
                        val baseColor = ColorHSLa(i * 90.0, 1.0, 0.8).toRGBa()
                        drawer.stroke = ColorRGBa.WHITE
                        var current = p
                        // On each iteration, draw a line to the next point
                        getSourcePixel(p) { next ->
                            drawer.stroke = drawer.stroke!!.mix(baseColor, 0.5)
                            drawer.lineSegment(current, next)
                            current = next
                        }

                    }
                }
            }.also { println("time per frame: $it ms") }
        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
            }
            when (it.name) {
                "w" -> ang += angOff
                "s" -> ang -= angOff
                "a" -> scl += sclOff
                "d" -> scl -= sclOff
                "+" -> max_iter++
                "-" -> max_iter--

                "h" -> delta_fac += delta_fac_off
                "n" -> delta_fac -= delta_fac_off
                "b" -> delta_rot += delta_rot_off
                "m" -> delta_rot -= delta_rot_off

                "g" -> isShowingGrid = !isShowingGrid
                "c" -> isCornerDebugging = !isCornerDebugging
            }
        }

        extend {
            drawer.displayLinesOfText(listOf(
                "ang: $ang (+/- off: $angOff w,s)",
                "scl: $scl (+/- off: $sclOff a,d)",
                "max_iter: $max_iter (+/-: by 1 with +,-)",
                "delta_fac: $delta_fac (+/- $delta_fac_off with h,n)",
                "delta_rot $delta_rot (+/- $delta_rot_off with b,m)",
                "isShowingGrid: $isShowingGrid (toggle with g)",
                "isCornerDebugging $isCornerDebugging (toggle with c)"
                )
            )
        }
    }
}

inline fun ColorBufferShadow.forEachIndex(block: (Int) -> Unit) {
    this.download()
    for(i in 0 until size) { block(i) }
    this.upload()
}
private operator fun ColorBufferShadow.get(v: Vector2) : ColorRGBa {
    return this[v.x.toInt(), v.y.toInt()]
}

private operator fun ColorBufferShadow.set(v: Vector2, color: ColorRGBa) {
    this[v.x.toInt(), v.y.toInt()] = color
}
