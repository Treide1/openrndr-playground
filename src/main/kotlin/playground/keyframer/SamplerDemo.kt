package playground.keyframer

import org.openrndr.application
import org.openrndr.extra.keyframer.Keyframer
import org.openrndr.math.IntVector2

fun main() = application {
    configure {
        position = IntVector2(-800, 100)
    }
    program {

        val anim = object: Keyframer() {
            val position by Vector2Channel(arrayOf("x", "y"))
            val radius by DoubleChannel("radius")
            val color by RGBChannel(arrayOf("r","g","b"))
        }
        var time = 0.0

        anim.loadFromWatchedFile(program, ResFiles.BASIC_ANIM.path) {
            time = 0.0
        }

        val offAbs = 0.05
        var timeOff = offAbs

        extend {
            if (frameCount%4 == 0) timeOff *= -1
            time += deltaTime
            anim(time + timeOff)

            drawer.fill = anim.color
            drawer.circle(anim.position, anim.radius)
        }
    }
}