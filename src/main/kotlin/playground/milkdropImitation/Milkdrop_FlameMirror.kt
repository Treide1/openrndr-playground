package playground.milkdropImitation

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.fx.blur.GaussianBlur
import org.openrndr.extra.shadestyles.linearGradient
import org.openrndr.extra.shapes.grid
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import utils.CyclicFlag
import utils.TAU
import utils.displayLinesOfText
import utils.toR
import kotlin.math.sin

fun main() = application {
    configure {
        // Fullscreen config
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        // Init render target to draw on
        val rt = renderTarget(width, height) {
            colorBuffer()
        }
        val drawBuffer = rt.colorBuffer(0)

        // Init stencil buffer
        val stencilTarget = renderTarget(width, height) {
            colorBuffer(format = ColorFormat.R, type = ColorType.UINT8)
        }
        val stencilBuffer = stencilTarget.colorBuffer(0)

        // Init single mirror filter
        val mirrorFx = MirrorFlameStencilFilter(stencilBuffer)

        // Init flame var effects
        val maxFx = 8
        val fx1 = CyclicFlag((0..maxFx).toList())
        val fx2 = CyclicFlag((0..maxFx).toList())

        // Init VFX
        var postFxEnabled = true
        val blurFx = GaussianBlur().apply {
            window = 5
            sigma = 1.0
        }
        val bloomFx = GaussianBloom().apply {
            window = 5
            sigma = 1.0
            gain = 0.4
        }


        extend {
            // Draw the upper half of the screen red, the lower half blue. Add some grid lines for orientation.
            drawer.isolatedWithTarget(rt) {
                clear(ColorRGBa.TRANSPARENT)
                rt.clearDepth(stencil = 0)

                // Draw a linear gradient covering the screen
                val rect = drawer.bounds
                shadeStyle = linearGradient(
                    ColorRGBa(1.0, 0.0, 0.0, 1.0),
                    ColorRGBa(0.0, 1.0, 0.0, 1.0)
                )
                drawer.rectangle(rect)
                shadeStyle = null

                // Draw a grid overlay
                bounds.grid(5, 5).flatten().forEach { r ->
                    stroke = ColorRGBa.BLACK
                    strokeWeight = 2.0
                    fill = null
                    rectangle(r)
                }
            }

            val circInterval = 10.0
            val xRange = width*0.2..width*0.45
            val circX = sin(seconds * TAU / circInterval).map(-1.0, 1.0, xRange.start, xRange.endInclusive)
            val circY = height * 0.5
            val circR = height * 0.2

            // Draw the stencil buffer
            drawer.isolatedWithTarget(stencilTarget) {
                // Clear the buffer
                clear(0.toR())

                // Draw rect on the ledt half of the screen
                fill = (128 + fx1.value).toR()
                stroke = null
                rectangle(Rectangle(0.0, 0.0, width*0.5, height * 1.0))

                // Draw a circle that moves left and right
                val centerCircle = Circle(circX, circY, circR)
                fill = (128 + fx2.value).toR()
                stroke = null
                circle(centerCircle)

                // Draw a mirror rectangle on the right side
                fill = 1.toR()
                stroke = null
                rectangle(Rectangle(width*0.5, 0.0, width*0.5, height * 1.0))
            }

            // Apply the mirror filter
            mirrorFx.apply(drawBuffer, drawBuffer)

            // Apply VFX
            if (postFxEnabled) {
                blurFx.apply(drawBuffer, drawBuffer)
                bloomFx.apply(drawBuffer, drawBuffer)
            }

            // Draw the render targets color buffer to the screen
            drawer.image(drawBuffer)

            // Display some values for debugging,
            // namely: flameOff, fx1, fx2, circX, circY, circR
            val fxName1 = MirrorFlameStencilFilter.flameVarNames[fx1.value]
            val fxName2 = MirrorFlameStencilFilter.flameVarNames[fx2.value]
            drawer.fill = ColorRGBa.WHITE
            drawer.displayLinesOfText(
                listOf(
                    "fx1 for background: ${fxName1.padEnd(12)} (press 1 to cycle)",
                    "fx2 for circle:     ${fxName2.padEnd(12)} (press 2 to cycle)",
                    "fade on/off:        ${mirrorFx.fade.toString().padEnd(12)} (press f to toggle)",
                    "postFx on/off:      ${postFxEnabled.toString().padEnd(12)} (press p to toggle)",
                    "",
                    "circX: $circX"
                ),
                20.0,
                20.0
            )
        }

        // On ESCAPE, exit the program
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
            }
            when (it.name) {
                "1" -> fx1.next()
                "2" -> fx2.next()
                "f" -> mirrorFx._fade = !mirrorFx._fade
                "p" -> postFxEnabled = !postFxEnabled
            }
        }
    }
}

// Filter class for mirroring a portion of the screen from this frame
class MirrorFlameStencilFilter(
    stencilBuffer: ColorBuffer
) : Filter(
    filterShaderFromCode(resourceText("/mirrorFlameVars.glsl"), "mirrorFlameVars")
) {
    var stencil by parameters
    var yScl by parameters
    var fade by parameters

    var _fade = true
        set(value) {
            field = value
            fade = value
        }

    init {
        stencil = stencilBuffer
        yScl = stencilBuffer.width.toDouble() / stencilBuffer.height.toDouble()
        fade = _fade
    }

    companion object {
        val flameVarNames = listOf(
            "identity",
            "sinusoidal",
            "spherical",
            "swirl",
            "horseshoe",
            "polar",
            "handkerchief",
            "heart",
            "disc"
        )
    }

}

