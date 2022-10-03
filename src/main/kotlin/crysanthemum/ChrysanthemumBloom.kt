package crysanthemum

import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.DEEP_PINK
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.contour
import utils.PHI
import utils.sqrt
import utils.toDegrees
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.system.exitProcess

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
        fun petalScl() = Vector2(5.0, 2.5)

        fun emissionRate() = (1000L / (125.0 / 60)).toLong() // emit a petal at this rate in ms
        fun emissionLifetime() = 12000L // lifetime of each petal in ms
        fun maxPetals() = emissionLifetime() / emissionRate()
        fun startRad() = 20.0
        fun endRad() = 150.0

        // Fibonacci Spiral Emission

        var petalsEmitted = 0

        class PetalAnim : Animatable() {
            // Constant
            val theta = 2*PI/(PHI * PHI) * petalsEmitted++
            val deg = theta.toDegrees()
            val er = emissionRate()
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
                ::brg.animate(1.0, er, Easing.CubicIn)
                ::brg.complete()
                ::brg.animate(1.0, lt - 4*er)
                ::brg.complete()
                ::brg.animate(0.0, 3*er, Easing.SineInOut)
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

                val scl = petalScl()
                val v = Polar(theta.toDegrees(), r).cartesian.rotate(90.0)
                    .map(Vector2.ZERO, Vector2.ONE, Vector2.ZERO, scl)
                moveOrLineTo(v)
            }
            close()
        }

        // Draw Petals

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
                    fill = ColorRGBa.DEEP_PINK.opacify(anim.brg)

                    translate(width/2.0, height/2.0)

                    rotate(anim.deg)
                    val x = 0.0
                    val y = anim.radius
                    translate(x,y)

                    contour(petalContour)
                }
            }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR) {
                petalAnimatables += PetalAnim()
                time = 0.0
            }
            if (it.key == KEY_ESCAPE) exitProcess(0)
        }

    }
}
