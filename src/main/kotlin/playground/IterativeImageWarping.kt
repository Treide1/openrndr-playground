package playground

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.renderTarget


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
 * The eigenvectors are v with lv = Av with eigenvalue l.
 *      <=> (A-lI)v = 0
 * eig(A): det ((a-l b)(c d-l)) = 0 <=> (a-l)*(d-l) - bc = det A - l*trace A + l^2 = 0
 *      <=> ... [See clever people's algorithm]
 *
 * If all |l| > 1 resolve differently.
 */

fun main() = application {
    val w = 640
    val h = 480
    val size = w*h
    configure {
        width=2*w
        height=h
    }
    program {
        // Warp func
        val V = { x: Int, y:Int  ->
            val xN = (w-x)/2
            val yN = (h-y)/2
            xN + yN*w
        }
        fun G(x_w: Int): (Int) -> Int = { x_s: Int ->
            x_w - V(x_s%w, x_s/w)
        }

        val rt = renderTarget(w, h) {
            colorBuffer("source")
            colorBuffer("warped")
            colorBuffer("accumulated")
        }
        val sourceShadow = rt.colorBuffer(0).shadow
        val warpedShadow = rt.colorBuffer(1).shadow

        val delta_x = -100
        val max_iter = 20
        fun getSourcePixelColor(x_w: Int) : ColorRGBa {
            // Preprocess contraction
            val contraction = G(x_w)
            // Pick x_0
            var currentX = x_w+delta_x
            repeat(max_iter) {
                currentX = contraction(currentX)
            }
            var x_s = currentX
            if (x_s < 0 || x_s >= size) x_s = -1
            return if (x_s != -1) sourceShadow[x_s%w, x_s/w] else ColorRGBa.BLACK
        }

        extend {
            // Interactive blending value
            val relAlpha = mouse.position.y / h

            // Draw on raw
            sourceShadow.download()
            for (x_s in 0 until size) {
                val x = x_s % w
                val y = x_s / w
                sourceShadow[x, y] = ColorRGBa(x.toDouble()/w, y.toDouble()/h, 1.0)
            }
            sourceShadow.upload()

            // Draw on warped
             warpedShadow.download()
            for (x_w in 0 until size) {
                val x = x_w % w
                val y = x_w / w
                val c = getSourcePixelColor(x_w)
                warpedShadow[x, y] = c
            }
            warpedShadow.upload()

            drawer.image(rt.colorBuffer(0), 0.0, 0.0)
            drawer.image(rt.colorBuffer(1), w.toDouble(), 0.0)
        }
    }
}

// TODO: refactor to use this data class to do unambiguous maths
data class Pixel(val x: Int, val y: Int)

