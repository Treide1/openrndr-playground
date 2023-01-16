package external.templates

import org.openrndr.application
import org.openrndr.extra.midi.MidiDeviceDescription

fun main() = application {
    program {
        MidiDeviceDescription.list().forEach {
            println("name: '${it.name}', vendor: '${it.vendor}', receiver:${it.receive}, transmitter:${it.transmit}")
        }
    }
}
