package chrysanthemum

import org.openrndr.application

/**
 * BeatEnvelope driven rings of flower petals.
 *
 * Switch between different envelopes highlighting beat patterns.
 *
 * Ideas for patterns:
 * 1-2-3-4- (Four on the floor)
 * --2---4- (Clap)
 * -+-+-+-+ (Off beat)
 * 1------- (Key beat)
 *
 * The four rings can be individually toggled, but not their individual petals.
 *
 * Author: Lukas Henke, 03.10.2022
 */
fun main() = application {
    configure {
        width = 1000
        height = 1000
        title = "Beat Petal Rings"
    }
    program {
        extend {

        }
    }
}