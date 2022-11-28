package playground

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.ffmpeg.loadVideoDevice

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }

    program {
        val camera = loadVideoDevice()
        camera.play()

        val comp = compose {
            layer {
                draw {
                    val cameraImage = camera.colorBuffer
                    if (cameraImage != null) {
                        drawer.imageFit(cameraImage, drawer.bounds)
                    }
                }
            }
        }

        extend(Screenshots())
        extend {
            camera.draw(drawer, blind = false)
            comp.draw(drawer)
        }

        keyboard.keyDown.listen {
            if(it.key == KEY_ESCAPE) application.exit()
        }
    }
}