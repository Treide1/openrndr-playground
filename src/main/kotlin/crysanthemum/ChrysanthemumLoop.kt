import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.math.Polar
import org.openrndr.math.map
import utils.circularSublist
import utils.toDegrees
import kotlin.math.*

fun main() = application {
    configure {
        width = 1000
        height = 1000
        //windowResizable = true
        //fullscreen = Fullscreen.SET_DISPLAY_MODE
        title = "Chrysanthemum Loop"
    }

    program {
        // Petal
        val n = 5
        val pointCount = 40
        val petalPoints = (0 until pointCount).map {
            var radians = it.toDouble().map(0.0, pointCount.toDouble(), 0.0, PI/n)
            radians += PI/2.0 / n

            radians += frameCount*0.01

            val r = cos(n*radians)

            val vec = Polar(radians.toDegrees(), r).cartesian
            return@map vec * (width/2.0-100)
        }
        val flowerPoints = (0 until n).map {
            val rotOff = it.toDouble().map(0.0, n.toDouble(), 0.0, 2*PI)
            petalPoints.map { v2 ->
                v2.rotate(rotOff.toDegrees())
            }
        }.flatten()

        var pointOff = 0
        var isPaused = false

        extend {
            if (frameCount % 3 == 0 && !isPaused) pointOff ++
            pointOff %= flowerPoints.size

            drawer.isolated {
                text("pointOff = $pointOff", 20.0, 20.0)
            }
            drawer.isolated {
                translate(width/2.0, height/2.0)
                stroke = ColorRGBa.WHITE
                fill = ColorRGBa.WHITE
                lineStrip(flowerPoints.circularSublist(pointOff, pointOff+pointCount))
            }
        }

        keyboard.keyDown.listen { stroke ->
            if (stroke.key == KEY_SPACEBAR) pointOff++
            if (stroke.name == "p") isPaused = !isPaused
        }
    }
}


