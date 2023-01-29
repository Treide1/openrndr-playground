package playground.filterFx

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa.Companion.BLUE
import org.openrndr.color.ColorRGBa.Companion.PINK
import org.openrndr.color.ColorRGBa.Companion.TRANSPARENT
import org.openrndr.color.ColorRGBa.Companion.WHITE
import org.openrndr.draw.isolated
import org.openrndr.extra.compositor.blend
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle
import org.openrndr.shape.compound
import utils.displayLinesOfText
import utils.map
import utils.vh
import utils.vw
import kotlin.math.absoluteValue

fun main() = application {
    configure { }
    program {

        // FILTERS
        val iMin = -8
        val iMax = 8
        val iRange = (iMin .. iMax)

        var filterIndex = 0
        val filterValues = FilterRef.values()
        val filterSize = filterValues.size

        fun getFilterRef(): FilterRef {
            return filterValues[(filterIndex + filterSize) % filterSize]
        }

        // LAYER COMPOSITION
        fun getComposite() = compose {
            draw {
                drawer.clear(TRANSPARENT)
            }

            val filter = getFilterRef().filter
            for(i in iRange) {
                layer {
                    val x = i.map(iMin, iMax, vw(.2), vw(.8))
                    val y = i.absoluteValue.map(0, iMax, vh(.5), vh(.8))
                    val rad = i.absoluteValue.map(0, iMax, 30.0, 80.0)
                    val mixValue = i.map(iMin, iMax, 1.0, .0)

                    val innerOff = 5.0

                    blend(filter)

                    val com = compound {
                        difference {
                            shape(Rectangle(0.0,0.0, rad).shape)
                            shape(Rectangle(innerOff,innerOff, rad-2*innerOff).shape)
                        }
                    }

                    draw {
                        drawer.isolated {
                            stroke = WHITE
                            fill = PINK.mix(BLUE, mixValue)
                            rotate(Vector3.UNIT_Z, 0.01 * mouse.position.x * i)
                            translate(x, y)
                            rotate(Vector3.UNIT_Z, 0.1 * mouse.position.y * i)
                            shapes(com)
                        }
                     }
                 }
             }
        }

        var composite = getComposite()

        extend {
            composite.draw(drawer)
            drawer.displayLinesOfText(listOf(
                "Controls:",
                "+, -, ESCAPE",
                "Values:",
                "FilterRef      - ${getFilterRef()}"
            ), 20.0, 20.0, 25.0)
        }

        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
            when(it.name) {
                "+" -> {
                    filterIndex++
                    composite = getComposite()
                }
                "-" -> {
                    filterIndex--
                    composite = getComposite()
                }
            }
        }
    }
}
