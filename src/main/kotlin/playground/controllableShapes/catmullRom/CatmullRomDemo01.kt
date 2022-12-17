package playground.controllableShapes.catmullRom

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.math.map
import kotlin.math.sin

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        val center = application.windowSize.times(.5)
        val a0 = 0.0
        val a1 = 180.0
        val rad = center.length*.5

        var alpha = .5
        val alphaInc = .1

        val catmullRomWedge = CatmullRomWedge(center, a0, a1, rad)

        extend {
            catmullRomWedge.clearRelPoints()
            val x = mouse.position.x / width
            val y = mouse.position.y / height

            catmullRomWedge.addRelativePoint(.25, 0.0)
            catmullRomWedge.addRelativePoint(
                x.map(0.0, 1.0 ,0.1, 0.9),
                y.map(0.0, 1.0, 1.0, -1.0)
            )
            catmullRomWedge.addRelativePoint(.75, 0.0)

            val off = 10.0 * sin(seconds*.5)

            catmullRomWedge.angleFrom = a0 + off
            catmullRomWedge.angleTo = a1 - off
            catmullRomWedge.alpha = alpha
        }

        extend {
            drawer.isolated {
                stroke = ColorRGBa.WHITE
                fill = ColorRGBa.PINK
                strokeWeight = 10.0
                lineJoin = LineJoin.ROUND

                shape(catmullRomWedge.shape)
            }
        }

        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
            when(it.name) {
                "+" -> alpha += alphaInc
                "-" -> alpha -= alphaInc
            }
        }
    }
}