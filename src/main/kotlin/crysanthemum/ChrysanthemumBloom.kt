package crysanthemum

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.DARK_RED
import org.openrndr.extra.color.presets.LIGHT_GREEN
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.contour
import utils.toDegrees
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Procedural bloom of petals.
 *
 * Starts with no visible petals. Petals fade in, moving from center to boundary.
 * Each new petal is offset by fixed angle.
 * Shading is based on this angle with angle offset.
 *
 * Petals fade out approaching the boundary. Central petals overlap outer petals.
 * Movement is "punchy" with circular easing abruptly stopping in fixed intervals.
 *
 * Author: Lukas Henke, 02.10.2022
 *
 * Sources:
 * http://nightmare.com/rushing/misc/fibonacci/ -> Fibonacci Spiral
 * https://socratic.org/precalculus/polar-coordinates/rose-curves -> Rose Curve
 */
fun main() = application {
    configure {
        width = 1000
        height = 1000
        title = "Chrysanthemum Bloom"
    }

    program {

        // Config
        fun spacingFac() = 60.0
        fun contentSize() = 100.0

        fun symmetryNum() = 5
        fun petalPoints() = 40

        // Fibonacci Spiral

        val phi = (1 + sqrt(5.0)) / 2.0
        val petalPolars = (1..15).map { n ->
            val theta = 2*PI/(phi * phi) * n
            val r = spacingFac() * sqrt(n.toDouble())

            Polar(theta.toDegrees(), r)
        }

        extend {

            petalPolars.forEachIndexed { index, polar ->
                drawer.isolated {
                    fill = ColorRGBa.BLACK
                    stroke = ColorRGBa.LIGHT_GREEN.mix( ColorRGBa.DARK_RED, index.toDouble()/petalPolars.size)

                    translate(width/2.0, height/2.0)

                    rotate(polar.theta)
                    val x = 0.0
                    val y = polar.radius
                    translate(x,y)

                    val cs = contentSize()
                    val off = Vector2(-cs/2.0, 0.0) // content offset
                    rectangle(off.x, off.y, cs)
                }
            }

        }

        // Rose Curve Petal

        val petalContour = contour {
            val n = symmetryNum()
            val a = contentSize()
            val p = petalPoints()
            val arc = PI/n // angular dist for each petal

            (0..p).forEach { i ->
                val theta = i.toDouble().map(0.0, p.toDouble(), -arc*.5, arc*.5)
                val r = a * cos(n*theta)

                val v = Polar(theta.toDegrees(), r).cartesian
                moveOrLineTo(v)
            }
            close()
        }

        extend {
            val cs = contentSize()

            drawer.translate(cs, cs)
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.PINK

            drawer.contour(petalContour)
        }

    }
}
