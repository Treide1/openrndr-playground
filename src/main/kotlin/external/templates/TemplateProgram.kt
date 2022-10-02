import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage

fun main() = application {
    configure {
        width = 1920
        height = 1080
        //windowResizable = true
        fullscreen = Fullscreen.SET_DISPLAY_MODE
        title = "OPENRNDR Example"
    }

    program {
        val image = loadImage("data/images/pm5544.png")
        val font = loadFont("data/fonts/default.otf", 640.0)

        extend {
            drawer.fill = ColorRGBa.WHITE
            drawer.fontMap = font
            drawer.text("OPENRNDR", width / 2.0, height / 2.0)
            drawer.circle(width/2.0, height/2.0, 5.0)
        }
    }
}
