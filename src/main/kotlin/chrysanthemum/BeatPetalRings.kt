package chrysanthemum

import bpm.BeatEnvelope
import bpm.BeatEnvelopeBuilder.Companion.buildBySegments
import bpm.BeatModulator
import bpm.Clock
import bpm.addEqual
import org.openrndr.KEY_SPACEBAR
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.math.smoothstep
import org.openrndr.shape.contour
import utils.toDegrees
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

/**
 * BeatEnvelope driven rings of flower petals.
 * Each ring rotates slowly at different angular velocities.
 * The distance to center is controlled by BeatEnvelope
 *
 * Switch between different envelopes highlighting beat patterns.
 *
 * Ideas for patterns:
 * 1-2-3-4- (Four on the floor)
 * --2---4- (Clap)
 * -+-+-+-+ (Off beat)
 * 1------- (Key beat)
 *
 * The four rings can be individually toggled, but not their individual petals.
 *
 * Author: Lukas Henke, 03.10.2022
 */
fun main() = application {
    configure {
        width = 1000
        height = 1000
        title = "Beat Petal Rings"
    }

    program {

        // Config
        fun contentSize() = 100.0

        fun symmetryNum() = 5
        fun petalPoints() = 40
        fun petalScl() = Vector2(2.5, 5.0)

        fun bpm() = 125.0 // Song: "Understatement" by "Solid Stone"
        fun startRad() = 20.0
        fun endRad() = 150.0
        fun beatRadFac() = .15

        fun innerPetalCount() = 8
        fun outerPetalCount() = 20
        fun stepPetalCount() = 4

        fun rotationFac() = 0.02

        val petalsPerRing = (innerPetalCount() .. outerPetalCount() step stepPetalCount()).map { it } // is [8, 12, 16, 20]
        val ringCount = petalsPerRing.size // is 4

        // BEAT MODULATOR

        val smooth = { x:Double -> smoothstep(0.0, 1.0, x) }
        val punch = { x:Double -> 1 - (1-x).pow(4)}

        val modulator = BeatModulator()


        modulator.envelopes[0] = BeatEnvelope(bpm(), 1).buildBySegments {
            segmentJoin(0.5, 1.0) via punch
            segmentJoin(1.0, 0.0) via punch
        }
        modulator.envelopes[1] = BeatEnvelope(bpm(), 2).buildBySegments {
            segmentJoin(.25, 1.0) via smooth
            segmentJoin(1.0, 0.0) via Easing.CubicInOut
        }
        modulator.envelopes[2] = BeatEnvelope(bpm(), 8).buildBySegments {
            segment(0.0, 0.25, 0.0, 1.0) via smooth
            segmentJoin(1.0, 0.0) via Easing.None // same as Easer.Linear(), default value
        }
        modulator.envelopes[3] = BeatEnvelope(bpm(), 16).buildBySegments {
            segmentJoin(8.0, 1.0) via punch
            segmentJoin(16.0, 0.0) via punch
        }

        modulator.weights[3].set(1.0)

        val clock = extend(Clock()) {
            add(modulator)
        }

        // SLOW ROTATION

        val rotIncList = petalsPerRing.map { n -> n * rotationFac() / petalsPerRing[0] }
        val rotOffList = MutableList(ringCount) { 0.0 }

        //ROSE CURVE PETAL

        val petalContour = contour {
            // Aliases for easy reading
            val n = symmetryNum()
            val a = contentSize()
            val p = petalPoints()
            val arc = PI /n // angular width for each petal

            // Mapping index to contour point
            (0..p).forEach { i ->
                val theta = i.toDouble().map(0.0, p.toDouble(), -arc*.5, arc*.5)
                val r = a * cos(n*theta)

                val scl = petalScl()
                val v = Polar(theta.toDegrees(), r).cartesian.times(scl)
                moveOrLineTo(v)
            }
            close()
        }

        extend {

            // Tick the increments
            rotOffList addEqual rotIncList

            // Get samples within first beat
            val radFacList = modulator.sampleList(0.0, (1 - 1.0/(petalsPerRing.size))*.5, petalsPerRing.size)
                .map { 1.0 + it*beatRadFac() }

            // 2-D for loop over (i,j) in [rings] x [petals in ring] drawn from back to front
            petalsPerRing.reversed().forEachIndexed { i, petalAmount ->
                val fracI = i.toDouble()/petalsPerRing.size
                (0 until petalAmount).forEach { j ->
                    val fracJ = j.toDouble()/petalAmount
                    drawer.isolated {
                        // Translate to center
                        translate(width/2.0, height/2.0)

                        // Basic petal pos in polar
                        var theta = j*360.0/petalAmount
                        var rad = fracI.map(0.0, 1.0, endRad(), startRad())
                        // Movement offset
                        theta += rotOffList[i]
                        rad *= radFacList[i]

                        rotate(theta)
                        translate(rad, 0.0)

                        stroke = ColorRGBa.BLACK
                        fill = ColorRGBa.PINK.shade(fracI*.5+.5)
                        contour(petalContour)
                    }
                }
            }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR) modulator.syncAll()
            when (it.name) {
                "1" -> modulator.pushTransition(listOf(1.0, 0.0, 0.0, 0.0), .3)
                "2" -> modulator.pushTransition(listOf(0.0, 1.0, 0.0, 0.0), .3)
                "3" -> modulator.pushTransition(listOf(0.0, 0.0, 1.0, 0.0), .3)
                "4" -> modulator.pushTransition(listOf(0.0, 0.0, 0.0, 1.0), .3)
            }
        }
    }
}