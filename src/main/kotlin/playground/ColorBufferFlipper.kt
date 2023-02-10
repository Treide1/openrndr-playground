package playground

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import utils.map

fun main() = application {
    configure { }
    program {

        val w = 640
        val h = 480
        val rt = renderTarget(w, h) {
            colorBuffer(type = ColorType.UINT8)
            colorBuffer(type = ColorType.UINT8)
        }

        var currentIndex = 0

        fun ColorBuffer.update() {
            val shadow = this.shadow

            val isFlashFrame = frameCount%120==0
            shadow.download()
            for (y in 0 until h) {
                for (x in 0 until w) {
                    shadow[x, y] = if (isFlashFrame) ColorRGBa.WHITE
                        else shadow[x, y].shade(0.98)
                }
            }
            shadow.upload()
        }

        extend {
            runBlocking {
                val frontBuffer = rt.colorBuffer(currentIndex)
                val backBuffer = rt.colorBuffer(1-currentIndex)
                currentIndex = 1 - currentIndex
                launch {
                    drawer.image(frontBuffer)
                }
                launch {
                    frontBuffer.copyTo(backBuffer)
                    backBuffer.update()
                }
            }
        }
    }
}
