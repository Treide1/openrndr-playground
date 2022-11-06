package external.discourse

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.noise.Random
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.*


fun main() = application {
    configure {
        width = 1000
        height = 1000
    }

    program {

        fun getFlow(start: Vector2, nSteps: Int, step: Double):ShapeContour {
            var p = start.copy()
            val points = List(nSteps){
                val v = Vector2(p.y, -p.x + 11.0 * p.y * (0.12 - p.x * p.x))
                p += v * step
                p
            }.map{
                Vector2(map(-2.0, 2.0, 0.0, width * 1.0, it.x), map(-2.0, 2.0, height * 1.0, 0.0,  it.y))
            }
            val shp = contour {
                moveTo(points[0])
                for (i in 1 until points.size){
                    lineTo(points[i])
                }
            }
            return shp
        }

        var time = 0.0
        val nSteps = 300
        val step = 0.02
        val nSize = 10

        val startPoints = mutableListOf<Vector2>()
        for (x in 0 .. nSize){
            for(y in 0 .. nSize){
                startPoints.add(Vector2(map(0.0, nSize * 1.0, -1.8, 1.8, x * 1.0 ),
                    map(0.0, nSize * 1.0, -1.8, 1.8, y * 1.0 )))
            }
        }

        val sh = startPoints.map{getFlow(it, nSteps, step)}
        extend {
            drawer.stroke = ColorRGBa.WHITE
            drawer.fill = null
            drawer.strokeWeight = 2.0
            val style = shadeStyle {
                fragmentPreamble = """
                    #define linearstep(edge0, edge1, x) clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0)
                """.trimIndent()
                fragmentTransform = """
                    float t = c_contourPosition/p_l;
                    float alpha = fract(t - p_time -p_offset);
                    alpha = linearstep(0.92, 1.0, alpha);
                    vec3 col = mix(vec3(0.1, 0.24, 0.4), vec3(0.3, 0.2, 0.9), 1.0 - alpha * alpha);
                    x_stroke.rgb = mix(col, vec3(1.0), 1.0 - alpha * alpha);
                    x_stroke.a = alpha;
                """.trimIndent()
            }


            sh.forEachIndexed {i, c  ->
                drawer.shadeStyle = style
                style.parameter("time", time)
                style.parameter("l", c.length)
                style.parameter("offset",  Random.perlin(i * 0.303,  i * 0.808))
                drawer.contour(c)
            }

            time += 0.005
        }
    }
}