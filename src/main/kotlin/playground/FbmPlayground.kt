package playground

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.noise.Random.simplex
import org.openrndr.math.Vector2
import org.openrndr.math.map
import utils.map
import utils.vh
import utils.vw
import kotlin.math.abs
import kotlin.math.pow

fun main() = application {
    configure { }
    program {
        var yScl = 1.0
        val yFac = 1.2
        val n = 200
        val indices = (0..n)

        val octaves = 8
        val lacunarity = 0.2
        val gain = lacunarity
        val seed = 3

        val func = { x: Double ->
            var sum = 0.0
            var amp = 1.0
            val isPrinting = abs(x-0.5) < 0.01 && frameCount%100==0

            var lx = x
            for (i in 0 until octaves) {
                sum += (simplex((seed + i) * 0.1, lx) * amp).also {
                    if (isPrinting) println("i: $i, increment: $it, lx: $lx, amp: $amp, sum: $sum")
                }
                lx *= lacunarity
                amp *= gain
            }
            if (isPrinting) println("")
            sum
        }

        extend {

            val pos = mouse.position / drawer.bounds.dimensions
            val zoom = 2.0.pow(pos.x.map(0.0,1.0, 4.0, -10.0))
            val points = indices.map { i ->
                val rX = i.map(0,n,-0.45, 0.45)
                val fX = rX * zoom * 10
                val fY = func(fX)
                val rY = fY / zoom

                val x = vw(rX)
                val y = vh(rY*yScl)

                Vector2(x,y)
            }
            val midY = points[n/2].y

            drawer.isolated {
                stroke = ColorRGBa.WHITE
                translate(width/2.0, height/2.0)
                translate(0.0, -midY)
                points.forEach { (x,y) ->
                    circle(x,y, 10.0)
                }

            }

        }

        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
            }
            when (it.name) {
                "+" -> yScl *= yFac
                "-" -> yScl /= yFac
            }
        }
    }
}