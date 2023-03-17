package playground.keyframer

import kotlinx.serialization.json.put
import org.openrndr.application
import playground.keyframer.json.AnimationParser
import playground.keyframer.json.AnimationParser.Easing.*
import java.io.File

fun main() = application {
    configure { }
    program {

        val MyParser = object: AnimationParser() {
            var x = "x"
            var y = "y"
            var radius = "radius"
            var r = "r"
            var g = "g"
            var b = "b"

            fun write(target: String) {
                val output = build {
                    step(time = 0.0, CUBIC_IN_OUT) {
                        put(x, 0.0)
                        put(y, 0.0)
                        put(radius, 100)
                        put(r, 0.1)
                        put(g, 0.5)
                        put(b, 0.2)
                    }
                    step(time = 4.0, easing = LINEAR) {
                        put(x, 200.0)
                        put(y, 100.0 via BACK_IN)
                    }
                    step(6.0, CUBIC_IN_OUT) {
                        put(r, 0.8)
                        put(g, 0.2)
                        put(b, 0.6)
                    }
                }

                var currentPath = System.getProperty("user.dir")
                val pathSegs = listOf("data", "keyframes")
                pathSegs.forEach {
                    currentPath+="\\"+it
                    val f = File(currentPath)
                    if (f.exists().not()) {
                        f.mkdir()
                    }
                }
                currentPath+="\\"+target
                val f = File(currentPath)
                f.createNewFile()
                f.writeText(output)
            }
        }
        MyParser.write("persistedAnim.json")
        application.exit()

    }
}