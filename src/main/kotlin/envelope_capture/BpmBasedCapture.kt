package playground

import bpm.BeatEnvelope
import bpm.BeatEnvelopeBuilder.Companion.buildBySegments
import bpm.Clock
import envelope_capture.MouseCapture
import envelope_capture.beatsToSeconds
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.GAINSBORO
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import org.openrndr.shape.contour

/**
 * Capture single dimension mouse movement.
 * Captured movement is taken as an envelope for a periodic BeatEnvelope.
 *
 * Beats per loop is estimated by the length of the capture,
 * rounded to the next integer of beats.
 *
 * Continuum is filled with linear interpolation.
 *
 * Display as 2 rectangles in horizontal layout moving in y-range, up and down.
 */
fun main() = application {
    configure {
        width=1000
        height=800
    }

    program {

        // BeatEnvelope Setup
        val bpm = 125.0
        val beatsPerLoop = 4
        val absEnv = BeatEnvelope(bpm, beatsPerLoop).buildBySegments {
            segmentJoin(1.0, 1.0)
            segmentJoin(2.0, 0.0)
            segmentJoin(3.0, 1.0)
            segmentJoin(4.0, 0.0)
        } // absolute envelope
        var capEnv = BeatEnvelope(bpm, beatsPerLoop) // captured envelope

        val clock = extend(Clock()) {
            add(absEnv)
            add(capEnv)
        }

        // Visual Setup
        val margin = 30.0
        val w = 150.0
        val h = 150.0

        val xLeft = margin
        val xRight = width - w - margin
        val yLower = height - 2*h - 2*margin
        val yUpper = margin
        val yDisplay = height - h - margin

        // Capturing Setup
        val mouseCapture = extend(MouseCapture(mouse)) {
            captureLength = beatsToSeconds(4, bpm)
        }

        mouseCapture.onCaptureStopped = {
            println("Size of captureEvents: ${captureEvents.size}")

            // Parse mouseMovement into BeatEnvelope
            // Currently assumes:
            //  1. Movement only in y direction
            //  2. Start pos is y0 -> 0.0
            //  3. Pos with max y1 -> 1.0
            //  4. Linear Mapping of all other y values
            //  5. Recording started exactly on beat

            // Transforming range
            val t0 = 0.0 // t from zero
            val t1 = beatsToSeconds(4, bpm) // t to 4 beats as seconds
            val y0 = captureEvents.first().pos.y // y from first captured y
            val y1 = captureEvents.minOf { it.pos.y } // y to "highest" captured y (min y of screen)

            // Linear Transformation
            val tScl = captureEvents.map { (it.t).map(t0, t1, 0.0, 4.0) }
            val yScl = captureEvents.map { (it.pos.y).map(y0, y1, 0.0, 1.0) }

            clock.remove(capEnv)
            capEnv = BeatEnvelope(bpm, beatsPerLoop).buildBySegments {
                for(i in 0 until captureEvents.size) {
                    if (i < 3 || i > captureEvents.size - 3) println("t, y: ${tScl[i]} ${yScl[i]}")
                    if (i == 3) println("t, y: ... ")
                    segmentJoin(tScl[i], yScl[i])
                }
                if (this.lastT <= beatsPerLoop.toDouble()) segmentJoin(beatsPerLoop.toDouble(), 0.0)
            }
            clock.add(capEnv)
        }

        // Left Rectangle for Comparison
        extend {
            // Absolute Envelope display
            drawer.isolated {
                val x = xLeft
                val y = absEnv.sample().map(0.0, 1.0, yLower, yUpper)
                fill = ColorRGBa.GAINSBORO
                rectangle(x, y, w, h)
            }

            // Show Sampling
            val samples = absEnv.sampleList(0.0, 1.0, 50)
            val rect = Rectangle(xLeft, yDisplay, w, h)
            drawer.displaySamplesInRect(samples, 0.0, 1.0, rect)
        }

        // Right rectangle with captured movement
        extend {
            // Recorded Envelope display
            drawer.isolated {
                val x = xRight
                val y = capEnv.sample().map(0.0, 1.0, yLower, yUpper)
                fill = ColorRGBa.GAINSBORO
                rectangle(x, y, w, h)
            }

            // Show Sampling
            val samples = capEnv.sampleList(0.0, 1.0, 50)
            val rect = Rectangle(xRight, yDisplay, w, h)
            drawer.displaySamplesInRect(samples, 0.0, 1.0, rect)
        }

        keyboard.keyDown.listen {
            if (it.name == "r") mouseCapture.start()
            if (it.key == KEY_SPACEBAR) clock.enabled = clock.enabled.not()
        }
        keyboard.keyUp.listen {
            if (it.name == "r") mouseCapture.stop()
        }

    }
}

fun Drawer.displaySamplesInRect(samples: List<Double>, lower: Double, upper: Double, boundary: Rectangle) {

    this.isolated {
        fill = null
        strokeWeight *= 2.5
        stroke = ColorRGBa.GRAY
        rectangle(boundary)
    }

    val con = contour {
        samples.forEachIndexed { i, v ->
            val x = i.toDouble() / (samples.size) * boundary.width + boundary.corner.x
            val y = (1-v).map(lower, upper, 0.0, boundary.height)  + boundary.corner.y
            moveOrLineTo(x, y)
        }
    }
    this.isolated {
        stroke = ColorRGBa.GRAY
        contour(con)
    }
}