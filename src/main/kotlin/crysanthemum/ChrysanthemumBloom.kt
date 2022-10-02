package crysanthemum

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Polar
import utils.toDegrees
import kotlin.math.*

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

        // Fibonacci Spiral

        val phi = (1 + sqrt(5.0)) / 2.0
        val petalPolars = (1..15).map { n ->
            val theta = 2*PI/(phi * phi) * n
            val r = spacingFac() * sqrt(n.toDouble())

            Polar(theta.toDegrees(), r)
        }

        extend {
            drawer.translate(width/2.0, height/2.0)
            drawer.stroke = ColorRGBa.GREEN
            drawer.fill = ColorRGBa.BLACK

            petalPolars.forEachIndexed { index, polar ->
                drawer.isolated {

                    stroke = stroke!!.mix( ColorRGBa.RED, index.toDouble()/petalPolars.size)
                    rotate(polar.theta)
                    val x = 0.0
                    val y = polar.radius
                    val cs = contentSize()
                    rectangle(x-cs*.5, y, cs)


                    fill = ColorRGBa.RED
                    stroke = ColorRGBa.RED
                    circle(x, y, 2.0)
                }
            }

        }
    }
}
