package playground.controllableShapes

import org.openrndr.application

fun main() = application {
    configure { }
    program {
        val center = application.windowSize.times(.5)
        val a0 = 0.0
        val a1 = 45.0
        val rad = center.length*.5
        val wedge = Wedge(center, a0, a1, rad)

        extend {
            drawer.wedge(wedge)
        }
    }
}