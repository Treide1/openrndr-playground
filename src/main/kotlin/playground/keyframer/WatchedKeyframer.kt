package playground.keyframer

import org.openrndr.application
import org.openrndr.extra.filewatcher.watchFile
import org.openrndr.extra.keyframer.Keyframer
import org.openrndr.math.IntVector2
import java.io.File

fun main() = application {
    configure {
        position = IntVector2(-800, 100)
    }
    program {

        class Animation(val filepath: String, var onFileChange: () -> Unit = {}): Keyframer() {

            val position by Vector2Channel(arrayOf("x", "y"))
            val radius by DoubleChannel("radius")
            val color by RGBChannel(arrayOf("r","g","b"))

            init {
                watchFile(File(filepath)) { file ->
                    println(file.absolutePath)
                    loadFromJson(file)
                    onFileChange()
                }
            }
        }

        val anim = Animation(ResFiles.BASIC_ANIM.path)
        var time = 0.0

        anim.onFileChange = {
            time = 0.0
        }


        extend {
            time += deltaTime
            anim(time)
            drawer.fill = anim.color
            drawer.circle(anim.position, anim.radius)
        }
    }
}

class Watching
