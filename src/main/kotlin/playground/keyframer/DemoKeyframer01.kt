package playground.keyframer

import org.openrndr.application
import org.openrndr.extensions.SingleScreenshot
import org.openrndr.extra.keyframer.Keyframer
import org.openrndr.resourceUrl
import java.io.File
import java.net.URL

fun main() = application {
    program {
        class Animation: Keyframer() {
            val position by Vector2Channel(arrayOf("x", "y"))
        }
        val animation = Animation()
        animation.loadFromJson(File(ResFiles.BASIC_ANIM.path))


        if (System.getProperty("takeScreenshot") == "true") {
            extend(SingleScreenshot()) {
                this.outputFile = System.getProperty("screenshotPath")
            }
        }
        extend {
            animation(seconds)
            drawer.circle(animation.position, 100.0)
        }
    }
}