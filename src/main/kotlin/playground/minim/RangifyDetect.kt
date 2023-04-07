package playground.minim

import ddf.minim.Minim
import ddf.minim.analysis.FFT
import ddf.minim.analysis.LanczosWindow
import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorRGBa.Companion.BLACK
import org.openrndr.color.ColorRGBa.Companion.GREEN
import org.openrndr.color.ColorRGBa.Companion.WHITE
import org.openrndr.draw.isolated
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.color.presets.HONEYDEW
import org.openrndr.extra.minim.minim
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import utils.smoothstep
import kotlin.math.log2
import kotlin.math.pow

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        val minim = minim()
        val lineIn = minim.getLineIn(Minim.MONO, 2048/2, 48000f)
        val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
        fft.window(LanczosWindow())

        val rangeList = mutableListOf(
            20.0,
            160.0,
            320.0,
            2500.0,
            5000.0,
            20000.0
        )
        val orgFirst = rangeList.first()
        val exponentFirst = log2(orgFirst)
        val orgLast = rangeList.last()
        val exponentLast = log2(orgLast)
        val freqToRel = { f: Float ->
            log2(f*1.0).map(exponentFirst, exponentLast, 0.0, 1.0)
        }

        val intensityBuffer = MutableList(rangeList.size-1) { 0.0 }

        //val main = Rectangle(width*0.1, height*0.9, width*0.8, -height*0.8)
        //val simple = Rectangle(width*0.1, height*0.3, width*0.4, -height*0.2)
        val simple = Rectangle(width*0.1, height*0.9, width*0.8, -height*0.8)
        val main = Rectangle(width*0.1, height*0.3, width*0.4, -height*0.2)

        val rows = 10
        val cols = rangeList.size-1
        val simpleGrid = List(cols) { i ->
            val w = simple.width * 1.0 / cols
            val x = simple.x + i * w

            List(rows) { j ->
                val h = simple.height * 1.0 / rows
                val y = simple.y + j * h

                Rectangle(x,y,w,h)
            }
        }

        val c = ColorRGBa.HONEYDEW

        extend(Screenshots()) {
            this.folder = null
            key = "s"
        }
        extend {
            fft.forward(lineIn.mix)

            rangeList.windowed(2, 1).forEachIndexed { index, pair ->

                val lowF = pair[0].toFloat()
                val highF = pair[1].toFloat()
                val avg = fft.calcAvg(lowF, highF)

                val scl = 1.0
                val off = 0.0
                val correctedAvg = ((avg * 0.05 * 3.0.pow(index) + off) * scl).smoothstep(0.0, 1.5)

                val curr = intensityBuffer[index]
                val next = if(curr < correctedAvg) correctedAvg else curr * 0.95
                intensityBuffer[index] = next

                drawer.isolated {
                    val x0 = main.fromU(freqToRel(lowF))
                    val x1 = main.fromU(freqToRel(highF))
                    val xCenter = (x0+x1)/2.0
                    val y0 = main.fromV(0.0)
                    val y1 = main.fromV(correctedAvg)

                    fill = c.shade(0.3)
                    stroke = WHITE
                    rectangle(x0, y0, x1-x0, y1-y0)

                    stroke = null
                    fill = c.shade(1.0)
                    val w = 20.0
                    rectangle(xCenter-w/2, y0, w, y1-y0)
                }
            }

            simpleGrid.forEachIndexed { i, rL ->

                val curr = intensityBuffer[i]

                rL.forEachIndexed { j, r ->
                    val relJ = j * 1.0 / rows

                    if (relJ <= curr) drawer.isolated {
                        stroke = BLACK
                        strokeWeight = 10.0
                        fill = WHITE.mix(GREEN, relJ + 0.2)
                        rectangle(r)
                    }
                }
            }

            drawer.isolated {
                stroke = WHITE
                strokeWeight = 2.0
                val yOff = 4.0
                lineSegment(simple.x, simple.y+yOff, simple.x + simple.width, simple.y+yOff)
            }
        }

        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }
    }
}

private fun Rectangle.fromU(u: Double): Double = x + u * width
private fun Rectangle.fromV(v: Double): Double = y + v * height
