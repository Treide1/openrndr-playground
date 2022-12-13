package envelopeCapture

import animation.SecondOrderDynamics
import bpm.BeatEnvelope
import bpm.BeatEnvelopeBuilder.Companion.buildBySegments
import bpm.BeatModulator
import bpm.Clock
import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.animatable.easing.QuadInOut
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.BLUE_VIOLET
import org.openrndr.extra.color.presets.MIDNIGHT_BLUE
import org.openrndr.math.map
import org.openrndr.math.smoothstep
import org.openrndr.shape.contour
import utils.map
import utils.vh
import utils.vw
import kotlin.math.PI
import kotlin.math.sin

/**
 * Introduce four envelopes,
 * that are individually captured by mouse movement.
 *
 * Every track is displayed as a left-moving line.
 * The modulation can be toggled for each curve.
 */

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }

    val c1 = ColorRGBa.MIDNIGHT_BLUE
    val c2 = ColorRGBa.BLUE_VIOLET
    fun colorAt(p: Double) : ColorRGBa = c1.mix(c2, p)

    val bpm = 117.0
    val sampleSize = 1000

    val yRelOff = .05

    var envSelection = 1

    program {

        val beatModulator = BeatModulator()
        for (i in 0 until 4) {
            beatModulator.envelopes[i] = BeatEnvelope(bpm, 4) .buildBySegments {
                segmentJoin(1.0, 1.0) via QuadInOut()
                segmentJoin(2.0, 0.0) via QuadInOut()
                segmentJoin(3.0, 1.0) via QuadInOut()
                segmentJoin(4.0, 0.0) via QuadInOut()
            }
        }

        extend(Clock()) { add(beatModulator) }

        fun interpretKinematicAsBeatEnvelope(tL: List<Double>, xL: List<Double>) : BeatEnvelope {
            val tOmitAfter = 4.0 - 0.200 // drop last 200 ms
            val tL = tL.dropLastWhile { it > tOmitAfter }
            val xL = xL.subList(0, tL.size)

            val size = tL.size
            val xFirst = xL.first()
            val sod = SecondOrderDynamics(
                1.0,
                0.6,
                0.0,
                xFirst
            )

            return BeatEnvelope(bpm, 4).buildBySegments {
                segment(0.0, tL.first(),xFirst, sod.update(tL.first(), xL[0]))
                for (i in 1 until size) {
                    val dt = tL[i] - tL[i-1]
                    segmentJoin(tL[i], sod.update(dt, xL[i]))
                }

                // Close last segment gracefully
                // Leave and enter with same value (x) and rise (v)
                val x0 = sod.yp
                val x1 = sod.y
                val x2 = xFirst
                val t0 = tL[tL.size-2]
                val t1 = tL.last()
                val t2 = 4.0
                val v01 = (x1 - x0) / (t1 - t0)
                val v12 = (x2 - x1) / (t2 - t1)
                val vScl = (v01/v12) // Scaled because normalized easing is assumed
                // Interpolation block of shape h(x) = (1-fac(t)) * a(x) + fac(t)*b(x)
                // Works as C1-Interpolation because
                // fac(0) = 0, fac(1) = 1, fac'(0) = 0 and fac'(1) = 0
                val block: (Double) -> Double = { t ->
                    val fac = smoothstep(0.0, 1.0, t)
                    (1-fac)*(t*vScl) + (fac)*(1)
                }
                segmentJoin(4.0, xFirst) via block
            }
        }

        val mouseCapture = extend(MouseCapture()) {
            captureLength = beatsToSeconds(4, bpm)
        }

        mouseCapture.onCaptureStopped = {
            // Transforming range
            val t0 = 0.0 // t from zero
            val t1 = beatsToSeconds(4, bpm) // t to 4 beats as seconds
            val y0 = captureEvents.first().pos.y // y from first captured y
            val y1 = captureEvents.minOf { it.pos.y } // y to "highest" captured y (min y of screen)

            // Linear Transformation
            val tScl = captureEvents.map { (it.t).map(t0, t1, 0.0, 4.0) }
            val yScl = captureEvents.map { (it.pos.y).map(y0, y1, 0.0, 1.0) }

            val targetEnv = envSelection-1
            beatModulator.envelopes[targetEnv] = interpretKinematicAsBeatEnvelope(tScl, yScl)//interpretNaiveAsBeatEnvelope(tScl, yScl)
        }

        extend {

            for (i in 0 until 4) {
                val relI = i.map(0, 3, .2, .8)
                val samples = beatModulator.envelopes[i]!!.sampleList(0.0, 4.0*3, sampleSize)

                contour {
                    moveOrLineTo(vw(1.0), vh(1.0))
                    lineTo(vw(0.0), vh(1.0))
                    samples.forEachIndexed { index, sample ->
                        val x = index.toDouble().map(0.0, sampleSize-1.0, vw(0.0), vw(1.0))

                        var dampFac = index.map(0, sampleSize-1, 0.0, 1.0)
                        dampFac = sin(dampFac*PI)
                        val dampenedSample = sample * dampFac
                        val y = dampenedSample.map(0.0, 1.0, vh(relI + yRelOff), vh(relI - yRelOff))
                        lineTo(x, y)
                    }
                    close()
                }.also {
                    drawer.fill = colorAt(relI)
                    drawer.contour(it)
                }
            }
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) application.exit()
            if (it.key == KEY_SPACEBAR) beatModulator.syncAll()
            when(it.name) {
                "1" -> envSelection = 1
                "2" -> envSelection = 2
                "3" -> envSelection = 3
                "4" -> envSelection = 4
                "r" -> mouseCapture.start()
                "p" -> beatModulator.isTicking = beatModulator.isTicking.not()
            }
        }

        keyboard.keyUp.listen {
            if (it.name == "r") mouseCapture.stop()
        }
    }

}