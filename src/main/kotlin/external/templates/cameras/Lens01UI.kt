package external.templates.cameras

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.draw.filterShaderFromCode
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.blur.FrameBlur
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.fx.grain.FilmGrain
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.ffmpeg.loadVideoDevice

fun main() = application {
    configure {
        width = 720
        height = 720
    }

    program {
        val camera = loadVideoDevice()
        camera.play()

        val gui = GUI()

        val comp = compose {
            layer {
                draw {
                    val cameraImage = camera.colorBuffer
                    if (cameraImage != null) {
                        drawer.imageFit(cameraImage, drawer.bounds)
                    }
                }
                post(FrameBlur()) {
                    this.blend = 0.05
                }
                post(FilmGrain())


            //post(Perturb())
            }
        }

        val screenshots = extend(Screenshots()) {
        }

        mouse.buttonDown.listen {
            screenshots.trigger()
        }

        extend {
            camera.draw(drawer, blind = false)
            comp.draw(drawer)
        }

        keyboard.keyDown.listen {
            if(it.key == KEY_ESCAPE) application.exit()
        }
    }
}