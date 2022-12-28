package external.templates.tridimensional

import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DepthTestPass
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.meshgenerators.boxMesh
import org.openrndr.math.Vector3

fun main() = application {
    configure {
        multisample = WindowMultisample.SampleCount(4)
    }
    program {
        val cube = boxMesh(140.0, 70.0, 10.0)

        extend {
            drawer.perspective(60.0, width * 1.0 / height, 0.01, 1000.0)
            drawer.depthWrite = true
            drawer.depthTestPass = DepthTestPass.LESS_OR_EQUAL

            drawer.fill = ColorRGBa.PINK
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                        vec3 lightDir = normalize(vec3(0.3, 1.0, 0.5));
                        float l = dot(va_normal, lightDir) * 0.4 + 0.5;
                        x_fill.rgb *= l; 
                    """.trimIndent()
            }
            drawer.translate(0.0, 0.0, -150.0)
            drawer.rotate(Vector3.UNIT_X, seconds * 15 + 30)
            drawer.rotate(Vector3.UNIT_Y, seconds * 5 + 60)
            drawer.vertexBuffer(cube, DrawPrimitive.TRIANGLES)
        }
    }
}
