package playground

import bpm.BeatEnvelope
import bpm.BeatEnvelopeBuilder.Companion.buildBySegments
import bpm.BeatModulator
import bpm.Clock
import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.animatable.easing.Linear
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import playground.CornerPos.*
import kotlin.math.pow

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }

    program {
        // SETUP
        val cellSize = 100.0

        val grid = drawer.bounds
            .grid(cellSize, cellSize, 50.0, 50.0, 10.0, 10.0)

        fun isDownSlanting(i: Int, j: Int) : Boolean = (i+j)%2==0

        // BEAT MODULATOR
        val bpm = 125.0 // Change to your favorite song's bpm

        val punch = { x:Double -> 1 - (1-x).pow(4)}
        val linear = Linear()

        val eps = 0.001
        val lCut = 0.0 + eps
        val hCut = 1.0 - eps
        val modulator = BeatModulator {
            envelopes[0] = BeatEnvelope(bpm, 4).buildBySegments {
                segment(0.0, 1.0, 0.0, hCut) via punch
                segmentJoin(2.0, hCut) via linear
                segmentJoin(3.0, hCut) via linear
                segmentJoin(4.0, 0.0) via punch
            }
            envelopes[1] = BeatEnvelope(bpm, 4).buildBySegments {
                segment(0.0, 1.0, lCut, lCut) via linear
                segmentJoin(2.0, 1.0) via punch
                segmentJoin(3.0, lCut) via punch
                segmentJoin(4.0, lCut) via linear
            }
        }

        val clock = extend(Clock()) {
            add(modulator)
        }

        // DRAW LOOP
        extend {
            grid.forEachIndexed { i, rectList ->
                rectList.forEachIndexed { j, rect ->
                    // Determine org vectors
                    val posPair = if (isDownSlanting(i,j)) (UP_LEFT to DOWN_RIGHT) else (DOWN_LEFT to UP_RIGHT)
                    val vecPair = posPair.toList().map { pos -> rect.cornerAt(pos) }
                    val a = vecPair[0]
                    val b = vecPair[1]

                    // Determine phase altered vectors
                    val phaseOff = i * 2.0.pow(-4)
                    val startFac = modulator.envelopes[0]!!.sample(phaseOff)
                    val endFac = modulator.envelopes[1]!!.sample(phaseOff)
                    val start = a.mix(b, startFac)
                    val end = a.mix(b, endFac)

                    // Draw the line
                    drawer.stroke = ColorRGBa.WHITE
                    drawer.strokeWeight = 10.0
                    drawer.lineCap = LineCap.ROUND
                    drawer.fill = ColorRGBa.PINK
                    drawer.lineSegment(start, end)
                }
            }
        }

        // KEY BINDS
        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> clock.enabled = !clock.enabled
            }
            when(it.name) {
                "+" -> modulator.tick(0.0, 0.1, 1)
            }
        }
    }
}

fun Rectangle.cornerAt(cornerPos: CornerPos) : Vector2 {
    val xOff = if (cornerPos.isLeft) 0.0 else dimensions.x
    val yOff = if (cornerPos.isUp) 0.0 else dimensions.y

    return corner + Vector2(xOff, yOff)
}

enum class CornerPos(val isLeft: Boolean, val isUp: Boolean) {
    UP_LEFT(true, true),
    UP_RIGHT(false, true),
    DOWN_LEFT(true, false),
    DOWN_RIGHT(false, false)
}