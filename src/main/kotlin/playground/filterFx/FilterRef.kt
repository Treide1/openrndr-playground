package playground.filterFx

import org.openrndr.draw.Filter
import org.openrndr.extra.fx.blend.*

enum class FilterRef(val filter: Filter) {
    ADD(Add()),
    COLOR_BURN(ColorBurn()),
    COLOR_DODGE(ColorDodge()),
    DARKEN(Darken()),
    DESTINATION_ATOP(DestinationAtop()),
    DESTINATION_IN(DestinationIn()),
    DESTINATION_OUT(DestinationOut()),
    HARD_LIGHT(HardLight()),
    LIGHTEN(Lighten()),
    MULTIPLY(Multiply()),
    MULTIPLY_CONTRAST(MultiplyContrast()),
    NORMAL(Normal()),
    OVERLAY(Overlay()),
    PASSTHROUGH(Passthrough()),
    SCREEN(Screen()),
    SOURCE_ATOP(SourceAtop()),
    SOURCE_IN(SourceIn()),
    SUBTRACT(Subtract()),
    XOR(Xor()),
}