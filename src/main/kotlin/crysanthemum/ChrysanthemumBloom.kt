package crysanthemum

import org.openrndr.animatable.Animatable
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.contour
import utils.PHI
import utils.showCoordinateSystem
import utils.sqrt
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
        fun contentSize() = 100.0

        fun symmetryNum() = 5
        fun petalPoints() = 40

        fun emissionRate() = 800L // emit a petal at this rate in ms
        fun emissionLifetime() = 12000L // lifetime of each petal in ms
        fun maxPetals() = emissionLifetime() / emissionRate()
        fun startRad() = 50.0
        fun endRad() = 250.0

        // Fibonacci Spiral Emission

        var petalsEmitted = 0

        class PetalAnim : Animatable() {
            // Constant
            val theta = 2*PI/(PHI * PHI) * petalsEmitted++
            val deg = theta.toDegrees()
            val lt = emissionLifetime()

            // Relative
            var relTime = 0.0 // goes from 0 to 1 over the course of lifetime
            var brg = 0.0 // brightness

            val mp = maxPetals()
            val sMp = sqrt(mp.toDouble()) // sqrt of mp
            val r1 = startRad()
            val r2 = endRad()

            val radius: Double
                get() = (relTime*mp).sqrt().map(0.0, sMp, r1, r2)

            init {
                ::relTime.animate(1.0, lt)
                ::brg.animate(1.0, lt.times(.1).toLong())
                ::brg.complete()
                ::brg.animate(1.0, lt.times(.8).toLong())
                ::brg.complete()
                ::brg.animate(0.0, lt.times(.1).toLong())
            }
        }
        val petalAnimatables = mutableListOf<PetalAnim>()

        var time = 0.0

        // Rose Curve Petal

        val petalContour = contour {
            val n = symmetryNum()
            val a = contentSize()
            val p = petalPoints()
            val arc = PI/n // angular width for each petal

            (0..p).forEach { i ->
                val theta = i.toDouble().map(0.0, p.toDouble(), -arc*.5, arc*.5)
                val r = a * cos(n*theta)

                val v = Polar(theta.toDegrees(), r).cartesian
                moveOrLineTo(v)
            }
            close()
        }

        // Draw Debug Rects and Coords

        extend {
            time += deltaTime
            val erDouble = emissionRate() / 1000.0
            if (time > erDouble) {
                time %= erDouble
                petalAnimatables += PetalAnim()
            }
            if (petalAnimatables.getOrNull(0)?.hasAnimations() == false) {
                petalAnimatables.removeAt(0)
            }

            petalAnimatables.forEach { anim ->
                anim.updateAnimation()
                drawer.isolated {
                    stroke = ColorRGBa.BLACK.opacify(anim.brg)
                    fill = ColorRGBa.WHITE.opacify(anim.brg)

                    translate(width/2.0, height/2.0)

                    rotate(anim.deg)
                    val x = 0.0
                    val y = anim.radius
                    translate(x,y)

                    val cs = contentSize()
                    val off = Vector2(-cs/2.0, 0.0) // content offset
                    rectangle(off.x, off.y, cs)

                    showCoordinateSystem(20.0)
                }
            }
        }

        // Draw Debug Petal

        extend {
            val cs = contentSize()

            drawer.translate(cs, cs)
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.PINK

            drawer.contour(petalContour)
        }

    }
}
