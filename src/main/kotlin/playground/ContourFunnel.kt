package playground

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Vector2
import org.openrndr.shape.SegmentJoin
import org.openrndr.shape.contour
import org.openrndr.shape.offset
import utils.displayLinesOfText
import utils.vh
import utils.vw
import kotlin.math.absoluteValue
import kotlin.math.sign

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {

        var angle = -15.0
        val angleInc = 2.5

        var offsetAmount = 0
        var offInc = 1
        var offsetContinueMode = false

        val joinStyleArray = SegmentJoin.values()
        var joinStyleIndex = 0

        fun getJoinStyle() : SegmentJoin = joinStyleArray[joinStyleIndex]

        val fixedPos1 = Vector2(500.0, 200.0) // felt like a good control point with angle = -20.0
        val fixedPos2 = Vector2(1400.0, 750.0) // angle = -15.0

        val pos = fixedPos2

        val start = Vector2(vw(0.0), vh(.45)) // left-center-point
        val end = Vector2(vw(1.0), vh(0.0))
        val rightLower = Vector2(vw(1.0), vh(.5))
        val leftLower = Vector2(vh(0.0), vh(.5))
        val flippedStart = start.copy(y = height - start.y)
        val flippedEnd = end.copy(y = height - end.y)

        var isOnlyUpperMode = false

        val cUpper = contour {
            moveTo(start)
            arcTo(pos.x, pos.y, angle, largeArcFlag = false, sweepFlag = false, end)
            lineTo(rightLower)
            lineTo(leftLower)
            lineTo(start)
            close()
        }

        val cFull = contour {
            moveTo(start)
            arcTo(pos.x, pos.y, angle, largeArcFlag = false, sweepFlag = false, end)
            lineTo(flippedEnd)
            arcTo(pos.x, pos.y, -angle, largeArcFlag = false, sweepFlag = false, flippedStart)
            close()
        }

        // Draw upper funnel part
        extend {

            if (offsetContinueMode) offsetAmount += offInc
            val c = if (isOnlyUpperMode) cUpper else cFull

            drawer.isolated {

                scale(.5)
                translate(vw(.5), vh(.5))

                stroke = ColorRGBa.PINK
                strokeWeight = 8.0
                fill = ColorRGBa.TRANSPARENT
                contour(c)

                stroke = ColorRGBa.BLUE
                for (i in 1..offsetAmount.absoluteValue) {
                    stroke = stroke!!.mix(ColorRGBa.BLACK, .005)
                    val off = c.offset(offsetAmount.sign*i*10.0, getJoinStyle())
                    contour(off)
                }

            }

            val text = listOf(
                "Controls:",
                "Mouse pos  - Used as control point",
                "+          - Angle += $angleInc",
                "-          - Angle -= $angleInc",
                "i          - +1 offset lines",
                "k          - -1 offset lines",
                "SPACEBAR   - Toggle offset mode on/off",
                "ESCAPE     - Close application.",
                "s          - Cycle to next JoinStyle",
                "u          - Toggle isUpperMode on/off",
                "",
                "Values:",
                "Control point  - (${pos.x.toInt()}, ${pos.y.toInt()})",
                "Angle          - $angle",
                "Offset amount  - $offsetAmount",
                "Offset Mode    - $offsetContinueMode",
                "JoinStyle      - ${getJoinStyle()}",
                "isUpperMode    - $isOnlyUpperMode"
            )
            drawer.displayLinesOfText(text, 20.0, 20.0)

        }

        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> offsetContinueMode = !offsetContinueMode
            }
            when(it.name) {
                "+" -> angle += angleInc
                "-" -> angle -= angleInc
                "i" -> {
                    offsetAmount++
                    offInc = 1
                }
                "k" -> {
                    offsetAmount--
                    offInc = -1
                }
                "s" -> {
                    joinStyleIndex = (joinStyleIndex+1)%joinStyleArray.size
                }
                "u" -> isOnlyUpperMode = !isOnlyUpperMode
            }
        }
    }
}