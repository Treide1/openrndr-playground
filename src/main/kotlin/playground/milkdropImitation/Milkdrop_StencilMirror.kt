package playground.milkdropImitation

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.extra.shapes.grid
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.Vector4
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import playground.MirrorFilter
import utils.CyclicFlag
import utils.TAU
import java.nio.ByteBuffer
import kotlin.math.sin

fun main() = application {
    configure {
        // 640x480 config
        // width = 640
        // height = 480
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {

        val dims = IntVector2(width, height)

        // Init render target to draw on
        val rt = renderTarget(width, height) {
            colorBuffer()
            colorBuffer("stencil", format = ColorFormat.RGBa, type = ColorType.UINT8)
        }
        val drawBuffer = rt.colorBuffer(0)
        val stencilBuffer = rt.colorBuffer(1)

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

            // -- create a buffer (on CPU) that matches size and layout of the stencil buffer
            val w = stencilBuffer.width
            val h = stencilBuffer.height
            val formatCompCount = stencilBuffer.format.componentCount
            val typeCompSize = stencilBuffer.type.componentSize

            val buffer = ByteBuffer.allocateDirect(w * h * formatCompCount * typeCompSize)

            val circInterval = 10.0
            val xRange = width*0.2..width*0.45
            val circX = sin(seconds * TAU / circInterval).map(-1.0, 1.0, xRange.start, xRange.endInclusive)
            val circY = height * 0.5
            val circR = height * 0.2
            val centerCircle = Circle(circX, circY, circR)
            // -- fill buffer with stencil data
            for (y in 0 until height) {
                for (x in 0 until width) {
                    for (c in 0 until 3) {
                        // Set value to 2 if on the right half,
                        // 1 if inside of circle,
                        // 0 else
                        val value = when {
                            x > width / 2 -> 2
                            centerCircle.contains(Vector2(x.toDouble(), y.toDouble())) -> 1
                            else -> 0
                        }
                        buffer.put(value.toByte())
                    }
                    buffer.put(128.toByte())
                }
            }

            buffer.rewind()

            stencilBuffer.write(buffer)


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
