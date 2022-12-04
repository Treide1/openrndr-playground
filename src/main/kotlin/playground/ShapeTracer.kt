package playground

import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Vector2
import utils.map

/**
 * ShapeTracer application.
 *
 * Draws a rect at your mouse position.
 * Draws a fading tracer away from the center.
 * Adheres to visual consistency:
 *
 * 1. No object popping in or out of existence.
 *
 * 2. No object suddenly changing position, shape or color.
 *
 * Key binds:
 * ESC  - Close application
 * p    - Pause/Unpause layer movement.
 */
fun main() = application {
    configure {}
    program {

        // Layer Setup
        val layerCount = 4
        val rtOrg = buildRenderTarget(width, height)
        val rtList = List(layerCount) {
            buildRenderTarget(width, height)
        }
        var layerOff = 0.0
        val layerInc = 0.005
        val layerMod = .25
        var isLayerOffPaused = false
        val center = Vector2(width/2.0, height/2.0)

        // Program Extension: Main
        extend {

            // Update layerOff, unless isLayerOffPaused=true
            if (!isLayerOffPaused) {
                layerOff = (layerOff + layerInc) % layerMod
            }

            // Draw the rectangles, each with its own renderTarget. image() draws on top !
            // Start with the most-offset, least-alpha rect.
            // End with the least-offset, most-alpha rect.
            rtList.forEachIndexed { i, rt ->
                val relI = i.map(0, 3, 1.0, 0.25)

                val fadeFac =  2.0
                val dtc = center-mouse.position // diff to center

                val offsetRelI = relI - layerOff
                val pos = mouse.position - dtc.times((1-offsetRelI)*fadeFac)

                // Draw the rect with pre-calculated pos and offset on rt.
                // Then display the rt's colorBuffer to the drawer.
                drawer.drawRectOnRt(rt, pos, offsetRelI)
                drawer.image(rt.colorBuffer(0))
            }

           // Draw a no-offset, full-alpha rect on top for visual consistency.
            drawer.drawRectOnRt(rtOrg, mouse.position, 1.0, true)
            drawer.image(rtOrg.colorBuffer(0))
        }

        // Minimal key binds:
        // ESC  - Close Application
        // p    - Pause/Unpause layer movement
        keyboard.keyDown.listen {
            if (it.key == KEY_ESCAPE) application.exit()
            if (it.name == "p") isLayerOffPaused = !isLayerOffPaused
        }
    }
}



/**
 * Single-responsibility renderTarget factory method.
 * Adds a colorBuffer as min-requirement for drawing.
 */
private fun buildRenderTarget(width: Int, height: Int) : RenderTarget {
    return renderTarget(width, height) {
        colorBuffer()
    }
}

/**
 * Custom infix for Int times Boolean. Treats boolean value as Int 0 or 1.
 */
private infix operator fun Double.times(b: Boolean): Double {
    return if (b) this else 0.0
}

/**
 * Encapsulates drawing a rect on a RenderTarget, with some config
 */
private fun Drawer.drawRectOnRt(rt: RenderTarget, pos: Vector2 = Vector2.ZERO, opacity: Double = 1.0, withClear: Boolean = true) {
    this.isolatedWithTarget(rt) {
        if (withClear) clear(ColorRGBa.TRANSPARENT)

        stroke = ColorRGBa.WHITE.opacify(opacity)
        fill = ColorRGBa.PINK.shade(.9).opacify(opacity)
        rectangle(-40.0+pos.x, -40.0+pos.y, 80.0, 80.0)
    }
}