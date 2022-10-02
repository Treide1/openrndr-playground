package playground

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.BLUE_VIOLET
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.Random
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.math.min

/**
 * A grid of rectangles. Outlines controlled by noise field.
 * At noise val 0.0, a rectangle has 0 outlines.
 * At noise val 1.0, a rectangle has 4 outlines.
 * At values in between, it has an outline for each 0.25,
 * and a gray outline with brightness mapping [.0, .25] to [0, 256] brightness.
 *
 * The noise field is made with 2-D Perlin noise.
 * The x-y-Plane moves in a random direction at constant velocity.
 * (The random angle is uniformly distributed in [0°, 360°].)
 */
fun main() = application {
    configure {
        width = 1280
        height = 640
        title = "WanderingNoiseFieldGUI EXAMPLE"
    }

    val cfg = object {

        @ColorParameter("targetCol", order = 5)
        var targetCol = ColorRGBa.BLUE_VIOLET

        @DoubleParameter("noiseValCenter", 0.0, 1.0, order = 20)
        var noiseValCenter = .5

        @DoubleParameter("noiseValSpread", 0.0, 2.0, order = 25)
        var noiseValSpread = 1.0

        @DoubleParameter("noiseFieldVelocity", 0.0, 2.0, order = 30)
        var noiseFieldVelocity = 1.0

        @DoubleParameter("noiseFieldAngle", 0.0, 360.0, order = 35)
        var noiseFieldAngle = 60.0

        @DoubleParameter("noiseFieldFac", 0.0, 1.0, order = 40)
        var noiseFieldFac = .01

        @DoubleParameter("gutter", 0.0, 20.0, order = 45)
        var gutter = 10.0

        @BooleanParameter("randomSegmentOrder", order = 50)
        var randomSegmentOrder = true

        @DoubleParameter("cellSize", 2.0, 100.0, order = 55)
        var cellSize = 40.0
    }

    program {

        val gui = GUI().apply {
            compartmentsCollapsedByDefault = false
            persistState = false
        }

        fun getGrid(): List<Rectangle> = drawer.bounds
            .grid(cfg.cellSize, cfg.cellSize, 20.0, 20.0, cfg.gutter, cfg.gutter).flatten()
        var grid = getGrid()

        fun getNoiseFac(): Double = cfg.noiseFieldFac
        var noiseFac = getNoiseFac()

        fun getDisplaceInc(): Vector2 = Vector2.fromPolar(Polar(cfg.noiseFieldAngle, cfg.noiseFieldVelocity))
        var displaceInc = getDisplaceInc()

        var displace = Vector2(0.0, 0.0)

        extend {

            displace += displaceInc * noiseFac

            grid.forEach { rect ->

                val rectSegs = with(rect.contour.segments) {
                    if (cfg.randomSegmentOrder) this.shuffled(kotlin.random.Random(rect.hashCode()))
                    else this
                }

                val noiseVal =  Random
                    .perlin(rect.center * noiseFac + displace) * cfg.noiseValSpread + (cfg.noiseValCenter - 0.5)
                var briBuffer = noiseVal * rectSegs.size

                rectSegs.forEach { seg ->
                    // Consume a normalized brightness value.
                    // If there is still 1.0 or more, consume 1.0,
                    // else consume the remaining brightness, bringing it to 0.0
                    val briConsumed = run {
                        val reduceBy = min(briBuffer, 1.0)
                        briBuffer -= reduceBy
                        reduceBy
                    }

                    drawer.stroke = cfg.targetCol.shade(briConsumed)
                    drawer.strokeWeight = 1.0

                    drawer.segment(seg)
                }
            }
        }

        gui.onChange { name, _ ->
            when (name) {
                "gutter" -> grid = getGrid()
                "cellSize" -> grid = getGrid()
                "noiseFieldFac" -> noiseFac = getNoiseFac()
                "noiseFieldAngle", "noiseFieldVelocity" -> displaceInc = getDisplaceInc()
            }
        }
        gui.add(cfg, "Config")
        extend(gui)
    }
}

