package external.templates.contours

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import org.openrndr.shape.offset
import kotlin.math.cos


// Modified to fit my coding style
fun main() {
    application {
        configure {
            fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
        }
        program {

            fun vw(x: Double): Double {
                return width*x
            }

            fun vh(y: Double): Double {
                return height*y
            }

            val p1 = Vector2(vw(.5), vh(.2))
            val cp2 = Vector2(vh(.25), vh(.4))
            val cp3 = Vector2(vw(.75), vh(.6))
            val p4 = Vector2(vw(.5), vh(.8))

            val c = contour {
                moveTo(p1)
                curveTo(cp2, cp3, p4)
            }
            extend {

                drawer.isolated {
                    stroke = ColorRGBa.PINK
                    strokeWeight = 2.0
                    lineJoin = LineJoin.ROUND

                    point(cp2)
                    point(cp3)
                    contour(c)
                    stroke = ColorRGBa.BLUE

                    for (i in -8..8) {
                        if (i==0) continue
                        val off = 0.0 //seconds + 0.5
                        val o = c.offset(i * 10.0 * cos(off))
                        contour(o)
                    }
                }

            }

            keyboard.keyDown.listen {
                if (it.key == KEY_ESCAPE) application.exit()
            }
        }
    }


}