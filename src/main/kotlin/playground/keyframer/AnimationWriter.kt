package playground.keyframer

import org.openrndr.application
import playground.keyframer.json.AnimationParser
import playground.keyframer.json.AnimationParser.Easing.*

fun main() = application {
    configure { }
    program {

        val MyParser = object: AnimationParser() {
            var x by serialize(0.0)
            var y by serialize(0.0)
            var radius by serialize(0)
            var r by serialize(0.0)
            var g by serialize(0.0)
            var b by serialize(0.0)


            fun write(target: String) {
                buildAndWrite(target) {
                    step {
                        time = 0.0
                        easing = LINEAR
                        x = 0.0
                        y = 0.0
                        radius = 100
                        r = 0.1
                        g = 0.5
                        b = 0.2
                    }
                    step {
                        time = 4.0
                        easing = LINEAR
                        x = 400.0 via SINE_IN_OUT
                        y = 100.0 via SINE_IN_OUT
                        r = r
                        g = g
                        b = b
                    }
                    step {
                        time = 8.0
                        easing = CUBIC_IN_OUT
                        r = 0.8
                        g = 0.2
                        b = 0.7
                    }
                }
            }
        }

        MyParser.write("persistedAnim.json")
        application.exit()

    }
}