package playground.milkdropImitation

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.extra.shapes.grid
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import utils.TAU
import kotlin.math.sin

fun main() = application {
    configure {
        // Fullscreen config
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {

        val dims = IntVector2(width, height)

        // Init render target to draw on
        val rt = renderTarget(width, height) {
            colorBuffer()
        }
        val stencilTarget = renderTarget(width, height) {
            colorBuffer(format = ColorFormat.R, type = ColorType.UINT8)
        }
        val drawBuffer = rt.colorBuffer(0)
        val stencilBuffer = stencilTarget.colorBuffer(0)

        // Init single mirror filter
        val mirrorStencilFilter = MirrorStencilFilter(stencilBuffer)

        extend {
            // Update filter
            mirrorStencilFilter.center = mouse.position / dims.vector2

            // Draw the upper half of the screen red, the lower half blue. Add some grid lines for orientation.
            drawer.isolatedWithTarget(rt) {
                clear(ColorRGBa.TRANSPARENT)
                rt.clearDepth(stencil = 0)

                // Draw a rect covering the entire screen with a shade style that maps uv coords to red-green
                val rect = drawer.bounds
                shadeStyle = linearGradient(
                    ColorRGBa(1.0, 0.0, 0.0, 1.0),
                    ColorRGBa(0.0, 1.0, 0.0, 1.0)
                )
                drawer.rectangle(rect)
                shadeStyle = null

                bounds.grid(5, 5).flatten().forEach { r ->
                    stroke = ColorRGBa.WHITE
                    strokeWeight = 0.5
                    fill = null
                    rectangle(r)
                }

                // Draw the stencil buffer to the screen
                //drawer.image(stencilBuffer)
            }

            val circInterval = 10.0
            val xRange = width*0.2..width*0.45
            val circX = sin(seconds * TAU / circInterval).map(-1.0, 1.0, xRange.start, xRange.endInclusive)
            val circY = height * 0.5
            val circR = height * 0.2
            val centerCircle = Circle(circX, circY, circR)

            drawer.isolatedWithTarget(stencilTarget) {
                clear(0.toR())

                fill = 1.toR()
                stroke = null
                circle(centerCircle)

                fill = 2.toR()
                stroke = 2.toR()
                rectangle(Rectangle(width*0.5, 0.0, width*0.5, height * 1.0))
            }

            // Apply the mirror filter
            mirrorStencilFilter.apply(drawBuffer, drawBuffer)

            // Draw the render targets color buffer to the screen
            drawer.image(drawBuffer)
        }

        // On ESCAPE, exit the program
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }
    }
}

// Filter class for mirroring a portion of the screen from this frame
class MirrorStencilFilter(
    stencilBuffer: ColorBuffer
) : Filter(
    filterShaderFromCode(resourceText("/mirrorByStencil.glsl"), "mirrorByStencil")
) {

    var stencil by parameters
    var center by parameters

    init {
        stencil = stencilBuffer
        center = Vector2(0.5, 0.2)
    }

}

/**
 * Convert an integer to a ColorRGBa with [this] as the red channel for UINT8 color buffers.
 */
private fun Int.toR(): ColorRGBa = ColorRGBa(r = this / 256.0, g = 0.0, b = 0.0)

