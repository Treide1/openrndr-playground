package playground

import bpm.BeatEnvelope
import bpm.BeatEnvelopeBuilder.Companion.buildBySegments
import bpm.Clock
import org.openrndr.CursorType
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.GAINSBORO
import org.openrndr.math.Vector2
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
        }
        var capEnv = BeatEnvelope(bpm, beatsPerLoop)

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

        // Recording Setup
        var isRecording = false
        val mouseMovement = mutableListOf<Pair<Double, Vector2>>()

        fun startRecording() {
            isRecording = true
            mouse.cursorVisible = false

            mouseMovement.clear()
        }

        fun stopRecording() {
            isRecording = false
            mouse.cursorVisible = true

            println("mouseMovement: $mouseMovement")
            println("size: ${mouseMovement.size}")
            // Parse mouseMovement into BeatEnvelope
            // Currently assumes:
            //  1. Movement only in y direction
            //  2. Start pos is y0 -> 0.0
            //  3. Pos with max y1 -> 1.0
            //  4. Linear Mapping of all other y values
            //  5. Recording started exactly on beat

            // Transforming range
            val t0 = mouseMovement[0].first
            val t1 = mouseMovement.last().first
            val y0 = mouseMovement[0].second.y
            val y1 = mouseMovement.minOf { it.second.y }

            // Linear Transformation
            val beatsPerSecond = bpm / 60.0
            val tScl = mouseMovement.map { (it.first - t0 + 0.001) *  beatsPerSecond }
            val yScl = mouseMovement.map { (it.second.y).map(y0, y1, 0.0, 1.0) }

            clock.remove(capEnv)
            capEnv = BeatEnvelope(bpm, beatsPerLoop).buildBySegments {
                for(i in 0 until mouseMovement.size) {
                    println("t, y: ${tScl[i]} ${yScl[i]}")
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

        // Capturing process
        extend {
            if (isRecording) {
                val pos = mouse.position

                drawer.isolated {
                    fill = ColorRGBa.RED
                    stroke = null
                    circle(pos, 10.0)
                }
                val t = program.application.seconds
                mouseMovement.add(Pair(t, pos))
            }
        }

        keyboard.keyDown.listen {
            if (it.name == "r") startRecording()
            if (it.key == KEY_SPACEBAR) clock.enabled = clock.enabled.not()
        }
        keyboard.keyUp.listen {
            if (it.name == "r") stopRecording()
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