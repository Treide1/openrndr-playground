package external.templates.noise

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.*
import kotlin.math.abs
import kotlin.math.sqrt

fun main() = application {
    program {
        extend {
            drawer.fill = ColorRGBa.PINK
            drawer.stroke = null
            val s = 0.0080
            val t = seconds
            for (y in 4 until height step 8) {
                for (x in 4 until width step 8) {
                    val areaScale = when {
                        t < 3.0 -> abs(fbm(100, x * s, y * s, t, ::perlinLinear)) * 16.0
                        t < 6.0 -> billow(100, x * s, y * s, t, ::perlinLinear) * 2.0
                        else -> rigid(100, x * s, y * s, t, ::perlinLinear) * 16.0
                    }
                    drawer.circle(x * 1.0, y * 1.0, sqrt(areaScale))
                }
            }
        }
    }
}
