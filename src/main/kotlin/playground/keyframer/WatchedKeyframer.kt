package playground.keyframer

import org.openrndr.Program
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

        class Animation: Keyframer() {
            val position by Vector2Channel(arrayOf("x", "y"))
            val radius by DoubleChannel("radius")
            val color by RGBChannel(arrayOf("r","g","b"))
        }

        val anim = Animation()
        var time = 0.0

        anim.loadFromWatchedFile(program, ResFiles.BASIC_ANIM.path) {
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

fun Keyframer.loadFromWatchedFile(program: Program, filepath: String, onFileChange: (file: File) -> Unit = {}) {
    program.watchFile(File(filepath)) { file ->
        loadFromJson(file)
        onFileChange(file)
    }
}