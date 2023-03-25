package external.templates.rabbit

import RabbitControlServer
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extensions.SingleScreenshot
import org.openrndr.extra.parameters.*
import org.openrndr.math.*
import utils.OS
import utils.getOS


fun main() = application {
    configure {
        width = 800
        height = 800
        position = IntVector2.ZERO
    }

    program {
        // -- this block is for automation purposes only
        if (System.getProperty("takeScreenshot") == "true") {
            extend(SingleScreenshot()) {
                this.outputFile = System.getProperty("screenshotPath")
            }
        }

        //val rabbit = RabbitControlServer()
        val channel = "LH99Test"
        val rabbithole = "wss://rabbithole.rabbitcontrol.cc/public/rcpserver/connect?key=$channel"
        val clipboardUrl = "https://rabbithole.rabbitcontrol.cc/client/index.html#$channel"
        val rabbit = RabbitControlServer(true, 10001, 8080, rabbithole)
        clipboard.contents = clipboardUrl

        //val font = loadFont("demo-data/fonts/IBMPlexMono-Regular.ttf", 20.0)
        val settings = object {
            @TextParameter("A string")
            var s: String = "Hello"

            @DoubleParameter("A double", 0.0, 10.0)
            var d: Double = 10.0
                set(value) {
                    field = clamp(value, 10.0, 100.0)
                }

            @BooleanParameter("A bool")
            var b: Boolean = true

            @ColorParameter("A fill color")
            var fill = ColorRGBa.PINK

            @ColorParameter("A stroke color")
            var stroke = ColorRGBa.WHITE

            @Vector2Parameter("A vector2")
            var v2 = Vector2(200.0,200.0)

            @Vector3Parameter("A vector3")
            var v3 = Vector3(200.0, 200.0, 200.0)

            @Vector4Parameter("A vector4")
            var v4 = Vector4(200.0, 200.0, 200.0, 200.0)

            @ActionParameter("Action test")
            fun clicked() {
                d += 10.0
                println("Clicked from RabbitControl")
            }
        }

        val settings2 = object {
            @DoubleParameter("Yet another double", 0.0, 1.0)
            var d: Double = 1.0
        }

        rabbit.add(settings)
        rabbit.add(settings2)
        extend(rabbit)
        extend {
            drawer.clear(if (settings.b) ColorRGBa.BLUE else ColorRGBa.BLACK)
            // drawer.fontMap = font
            drawer.fill = settings.fill
            drawer.stroke = settings.stroke
            drawer.circle(settings.v2, settings.d)
            drawer.text(settings.s, 10.0, 20.0)
            drawer.rectangle(200.0, 300.0, settings2.d  * 400, 300.0)
        }

        Runtime.getRuntime().run {
            if (getOS() == OS.MAC_OS_X) exec("open $clipboardUrl") // mac style open browser command
        }
    }
}