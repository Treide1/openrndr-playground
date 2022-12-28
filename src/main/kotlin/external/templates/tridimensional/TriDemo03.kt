package external.templates.tridimensional

import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.camera.Orbital
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle

fun main() = application {
    configure {
        multisample = WindowMultisample.SampleCount(4)
    }
    program {
        val cam = Orbital()
        cam.eye = Vector3.UNIT_Z * 150.0
        cam.camera.depthTest = false

        extend(cam)
        extend {
            drawer.fill = null
            drawer.stroke = ColorRGBa.PINK
            repeat(10) {
                drawer.rectangle(Rectangle.fromCenter(Vector2.ZERO, 150.0))
                drawer.translate(Vector3.UNIT_Z * 10.0)
            }
        }
    }
}
