package playground

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBufferShadow
import org.openrndr.draw.renderTarget
import org.openrndr.math.Vector2
import kotlin.system.measureTimeMillis


/**
 * Refs: https://la.disneyresearch.com/wp-content/uploads/Iterative-Image-Warping-Paper.pdf
 *
 * Explanation:
 * We keep an accumulative render target, that blends incoming partial results.
 * We generate a new result from a source image with a specified warping.
 *
 * A warping takes a source pixel x_s to a warped location x_w by offset, named V:x_s ->  off.
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
 * The eigenvalues of A for trace T = a+d, and determinant D = ad - bc are
 * l_1 = T/2 + (T^2/4-D)^1/2 ,
 * l_2 = T/2 - (T^2/4-D)^1/2 .
 * If all |l| > 1 are calculated, interpolate accordingly.
 */

// Globals
val w = 640
val h = 480

val center = Vector2(w/2.0, h/2.0)
val size = w*h

fun main() = application {

    // Convenience funcs
    fun Vector2.isInViewport(): Boolean = (x.toInt() in 0 until w) && (y.toInt() in 0 until h)
    fun Int.toPixelVec() : Vector2 {
        return Vector2((this % w).toDouble(), (this / w).toDouble()) // .also { println("toInt2d: $this -> $it") }
    }

    // Double panel viewport:
    // Left side is original, right side is warp result
    configure {
        this.width=2*w
        this.height=h
    }
    program {
        // Warp func
        fun getV() : (Vector2) -> Vector2 = { p:Vector2 ->
            val ang = mouse.position.y / h * 90.0
            (center - p).rotate(ang)
        }

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

        val delta_x = Vector2(-100.0, 0.0)
        val max_iter = 4
        fun getSourcePixelColor(x_w: Vector2) : ColorRGBa {
            // Preprocess contraction
            val warp = getV()
            // Pick x_0
            var currentX = x_w + delta_x
            // Iterate over contraction
            repeat(max_iter) {
                currentX = x_w - warp(currentX) // = fix point function
            }
            // Use source pixel
            val x_s = currentX
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
                }
                sourceShadow.upload()

                // Draw on warped
                warpedShadow.forEachIndex { i_w ->
                    val x_w = i_w.toPixelVec()
                    val c = getSourcePixelColor(x_w)
                    warpedShadow[x_w] = c
                }

                accumulatedShadow.forEachIndex { i_a ->
                    val c = warpedShadow[i_a%w, i_a/w]
                    accumulatedShadow[i_a%w, i_a/w] = c*0.5 + accumulatedShadow[i_a%w, i_a/w] * 0.98
                }

                drawer.image(rt.colorBuffer(0), 0.0, 0.0)
                drawer.image(rt.colorBuffer(2), w.toDouble(), 0.0)
            }.also { println("time per frame: $it ms") }
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
