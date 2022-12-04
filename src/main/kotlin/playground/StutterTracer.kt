@file:Suppress("UnnecessaryVariable")

package playground

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.Shape
import utils.TAU
import utils.vh
import utils.vw
import kotlin.math.abs
import kotlin.math.sin

/**
 * ShapeModel for drawing a morphing object (shape, position, color).
 *
 * @param shape Shape to be drawn
 * @param pos Position to draw on screen
 * @param vel Add this to position on update
 * @param brg Brightness with which to draw
 * @param brgInc Add this to brg on update. Use negative value to fade from 1.0 to 0.0
 * @param stroke Stroke color
 * @param fill Fill color
 */
data class ShapeModel(
    var shape: Shape = Circle(0.0,0.0, 10.0).shape, // shape to be drawn
    var pos: Vector2 = Vector2.ZERO, // position
    var vel: Vector2 = Vector2.ZERO, // velocity
    var brg: Double = 1.0, // brightness
    var brgInc: Double = -0.01, // brightness increment, here negative
    var stroke: ColorRGBa = ColorRGBa.WHITE,
    var fill: ColorRGBa = ColorRGBa.PINK.shade(.9)
)

/**
 * ShutterTracer application.
 *
 * Stutters a tracer behind the current position.
 * The current is either the mouse cursor or a lissajous curve position.
 * Tracers slide in the direction of the moving position and fade out.
 *
 * Allows for some tweaking, key binds are explained via HUD.
 */
fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {

        // BPM Setup
        val bpm = 120.0 // "Deep Chicken" by Soundquelle
        val beatInterval = (60.0 / bpm) * 1000 // time span of a beat in ms
        val quarterInterval = beatInterval / 4.0

        // ShapeModel Setup
        val shapeModelList = mutableListOf<ShapeModel>()
        val creationInterval = quarterInterval // Difference in time between creations in ms
        var tAcc = 0.0 // Accumulated time, gets reduced by creationInterval at creation

        // Morphing Setup
        // val baseShapeModel = ShapeModel()
        // val curShape = baseShapeModel.shape
        var lastPos = mouse.position
        val velFac = 0.01

        // Targeting Setup
        var targetStyle = "1"
        val targetRadiusMap = mapOf(
            "1" to 10.0,
            "2" to 20.0,
            "3" to 30.0
        )
        fun targetRadius(): Double  = targetRadiusMap[targetStyle]!!

        var currentRadius = targetRadius()

        // Modes Setup
        var isPaused = false
        var isCurveMode = false

        // Lissajous setup
        // Used for drawing Lissajous curve(s), see https://en.wikipedia.org/wiki/Lissajous_curve
        val ljInterval = beatInterval * 4 // time per loop
        var tLj = 0.0

        var a = 1.0
        var b = 1.0
        var phase = 0.0 // phase shift from 0.0 to 1.0
        val phaseInc = 0.125

        fun getPos() : Vector2 {
            return if (!isCurveMode) mouse.position else {
                val relT = tLj / ljInterval
                val phaseX = (a/b*relT + phase) * TAU
                val phaseY = relT * TAU
                val x = sin(phaseX)
                val y = sin(phaseY)
                Vector2(
                    x = x.map(-1.0, 1.0, vw(.2), vw(.8)),
                    y = y.map(-1.0, 1.0, vh(.2), vh(.8))
                )
            }
        }

        // Program Extension: Main
        extend {

            // Update, unless isPaused=true
            if (!isPaused) {

                // Update style, track current to target
                val radDist = targetRadius() - currentRadius
                currentRadius = if (abs(radDist) > 1.0) currentRadius + radDist * .05 else targetRadius()

                // Update lissajous curve
                tLj = (tLj + deltaTime*1000) // % ljInterval

                // Update each existing shapeModel, then remove the expired ones
                shapeModelList.forEach { shapeModel ->
                    shapeModel.apply {
                        pos += vel
                        brg += brgInc
                    }
                }
                shapeModelList.removeAll { it.brg < 0.0 }

                // Adding ShapeModels to shapeModelList, based on time passed
                tAcc += deltaTime*1000
                while (tAcc >= creationInterval) {
                    tAcc -= creationInterval

                    val rad = currentRadius
                    val shape = Circle(0.0, 0.0, rad).shape
                    val mPos = getPos()
                    val vel = (mPos - lastPos).times(velFac)
                    val newSM = ShapeModel(shape = shape, pos = mPos, vel = vel)
                    lastPos = mPos

                    shapeModelList += newSM
                }
            }

            // Draw each shapeModel
            shapeModelList.forEach { shapeModel ->
                drawer.draw(shapeModel)
            }
        }

        // Program Extension: Text Display
        extend {
            val x = 20.0
            var y = 20.0

            val linebreak = "\n"
            val dataLines = listOf(
                "isPaused: $isPaused",
                "isCurveMode: $isCurveMode",
                "a: $a",
                "b: $b",
                "phase: $phase"
            )
            drawer.isolated {
                dataLines.forEach { line ->
                    y += 30.0
                    text(line, x, y)
                }
            }
            y += 30.0
            val keyLines = ("KEY_ESCAPE -> Exit application\n" +
                    "KEY_SPACEBAR -> Reset time").split(linebreak) + (
                    "\"p\" -> Toggle isPaused \n" +
                    "\"1\", \"2\", \"3\" -> Set targetStyle\n" +
                    "\"a\" -> a++\n" +
                    "\"b\" -> b++\n" +
                    "\"c\" -> Toggle isCurveMode\n" +
                    "\"+\" -> Increment phase\n" +
                    "\"-\" -> Decrement phase").split(linebreak)
            drawer.isolated {
                keyLines.forEach { line ->
                    y += 30.0
                    text(line, x, y)
                }
            }
        }

        // Minimal key binds:
        // ESC  - Close Application
        // p    - Pause/Unpause layer movement
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> tAcc =0.0
            }
            when (it.name) {
                "p" -> isPaused = !isPaused
                "1", "2", "3" -> targetStyle = it.name
                "a" -> a++
                "b" -> b++
                "c" -> isCurveMode = !isCurveMode
                "+" -> phase = (phase + phaseInc) % 1.0
                "-" -> phase = (phase - phaseInc + 1.0) % 1.0
            }
        }
    }
}

/**
 * Encapsulates drawing a shape based on the [shapeModel].
 * This specifies the drawing implementation.
 */
private fun Drawer.draw(shapeModel: ShapeModel) {
    // Destruct for easy access
    val (shape, pos, _, brg, _, stroke, fill) = shapeModel

    // Draw implementation
    this.isolated {
        this.stroke = stroke.shade(brg)
        this.fill = fill.shade(brg)
        translate(pos)
        shape(shape)
    }
}