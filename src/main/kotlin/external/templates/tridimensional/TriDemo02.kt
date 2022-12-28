package external.templates.tridimensional

import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.camera.Orbital
import org.openrndr.extra.meshgenerators.boxMesh
import org.openrndr.math.Vector3

fun main() = application {
    configure {
        multisample = WindowMultisample.SampleCount(4)
    }
    program {
        val cube = boxMesh(140.0, 70.0, 10.0)
        val cam = Orbital()
        cam.eye = -Vector3.UNIT_Z * 150.0

        extend(cam)
        extend {
            drawer.fill = ColorRGBa.PINK
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                        vec3 lightDir = normalize(vec3(0.3, 1.0, 0.5));
                        float l = dot(va_normal, lightDir) * 0.4 + 0.5;
                        x_fill.rgb *= l; 
                    """.trimIndent()
            }
            drawer.vertexBuffer(cube, DrawPrimitive.TRIANGLES)
        }
    }
}
