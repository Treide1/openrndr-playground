package playground

import org.openrndr.application
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import utils.smoothstep
import kotlin.math.pow

/**
 * 4 dots vertically aligned in the horizontal center.
 * The first one starts to smoothstep from its position horizontally, wraping around the screen.
 * It ends up where it started, just going around the screen once.
 * On completion the dot beneath it does the same, using the same amount of time.
 * This all continues after the 4th one, restarting at the 1st one.
 */
fun main() = application {
    configure {
        width = 1280
        height = 640
        title = "ScreenWrap EXAMPLE"
    }

    program {

        val dotCount = 4
        val wrapLength = 240 // frame count each wrap takes
        val wrapVec = Vector2(width.toDouble(), 0.0) // wrap direction off

        val dotOrgs = drawer.bounds.grid(1, dotCount, 0.0, 0.0).flatten().map { it.center }

        extend {
            val framePhase = (frameCount % wrapLength).toDouble()
            val wrapCount = frameCount / wrapLength
            val off = Vector2(width * framePhase.smoothstep(0.0, wrapLength.toDouble()), 0.0)

            dotOrgs.forEachIndexed { i, v2 ->
                when {
                    i == (wrapCount%dotCount) -> {
                        drawer.circle(v2 + off, 20.0)
                        drawer.circle(v2 + off - wrapVec, 20.0)
                    }
                    else -> drawer.circle(v2, 20.0)
                }
            }
        }
    }
}
