package playground

import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Vector2
import utils.map

fun main() = application {
    configure {}

    // Program running
    program {

        val layerCount = 4
        val rtList = List(layerCount) {
            renderTarget(width, height) {
                colorBuffer()
            }
        }
        var layerOff = 0.0
        val layerInc = 0.005
        val layerMod = .25
        var isLayerOffPaused = false

        val center = Vector2(width/2.0, height/2.0)

        extend {
            if (!isLayerOffPaused) {
                layerOff = (layerOff + layerInc) % layerMod
            }

            rtList.forEachIndexed { i, rt ->
                val relI = i.map(0, 3, 1.0, 0.25)

                val fadeFac =  2.0
                val dtc = center-mouse.position // diff to center

                val offsetRelI = relI - layerOff
                val pos = mouse.position - dtc.times((1-offsetRelI)*fadeFac)

                drawer.drawRectOnRt(rt, pos, offsetRelI)
                if (i==rtList.indices.last) drawer.drawRectOnRt(rt, mouse.position, 1.0, false)

                drawer.isolated {
                    image(rt.colorBuffer(0))
                }
            }


        }

        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) application.exit()
            if (it.name == "p") isLayerOffPaused = !isLayerOffPaused
        }
    }
}

private operator fun Double.times(b: Boolean): Double {
    return if (b) this else 0.0
}

private fun Drawer.drawRectOnRt(rt: RenderTarget, pos: Vector2, opacity: Double, withClear: Boolean = true) {
    this.isolatedWithTarget(rt) {
        if (withClear) clear(ColorRGBa.TRANSPARENT)

        stroke = ColorRGBa.WHITE.opacify(opacity)
        fill = ColorRGBa.PINK.shade(.9).opacify(opacity)
        rectangle(-40.0+pos.x, -40.0+pos.y, 80.0, 80.0)
    }
}