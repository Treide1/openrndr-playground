package playground

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.poissonfill.PoissonFill
import kotlin.math.*

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        val c = compose {
            layer {
                draw {
                    drawer.stroke = null
                    drawer.fill = ColorRGBa.RED
                    drawer.circle((cos(seconds) * 0.5 + 0.5) * width, (sin(seconds * 0.5) * 0.5 + 0.5) * height, 20.0)
                    drawer.fill = ColorRGBa.PINK
                    drawer.circle((sin(seconds * 2.0) * 0.5 + 0.5) * width, (cos(seconds) * 0.5 + 0.5) * height, 20.0)

                    drawer.fill = ColorRGBa.BLACK
                    drawer.circle((sin(seconds * 1.0) * 0.5 + 0.5) * width, (cos(seconds * 2.0) * 0.5 + 0.5) * height, 20.0)
                }
                post(PoissonFill())
            }
            layer {
                // -- an extra layer just to demonstrate where the original data points are drawn
                draw {
                    drawer.stroke = ColorRGBa.WHITE
                    drawer.strokeWeight = 5.0
                    drawer.fill = ColorRGBa.RED
                    drawer.circle((cos(seconds) * 0.5 + 0.5) * width, (sin(seconds * 0.5) * 0.5 + 0.5) * height, 20.0)
                    drawer.fill = ColorRGBa.PINK
                    drawer.circle((sin(seconds * 2.0) * 0.5 + 0.5) * width, (cos(seconds) * 0.5 + 0.5) * height, 20.0)

                    drawer.fill = ColorRGBa.BLACK
                    drawer.circle((sin(seconds * 1.0) * 0.5 + 0.5) * width, (cos(seconds * 2.0) * 0.5 + 0.5) * height, 20.0)
                }
            }

        }
        extend {
            c.draw(drawer)
        }
        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }
    }
}
