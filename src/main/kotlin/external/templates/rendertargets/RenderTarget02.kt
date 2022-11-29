package external.templates.rendertargets

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget

fun main() = application {
    program {
        // -- build a render target with a single color buffer attachment
        val rt = renderTarget(400, 400) {
            colorBuffer()
        }

        extend {
            drawer.isolatedWithTarget(rt) {
                drawer.clear(ColorRGBa.BLACK)

                // -- set the orthographic transform that matches with the render target
                ortho(rt)

                drawer.fill = ColorRGBa.WHITE
                drawer.stroke = null
                drawer.rectangle(40.0, 40.0, 80.0, 80.0)
            }

            // -- draw the backing color buffer to the screen
            drawer.image(rt.colorBuffer(0))
        }
    }
}

