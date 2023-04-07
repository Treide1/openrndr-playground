package playground

import org.openrndr.application
import org.openrndr.color.ColorRGBa

fun main() = application {
    program {
        extend{
            drawer.clear(ColorRGBa.BLACK)
            drawer.fill = ColorRGBa.WHITE
            drawer.circle(drawer.bounds.center, 100.0)
        }
    }
}
