package playground.keyframer.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File
import kotlin.reflect.KProperty


open class AnimationParser {

    var currentList = mutableListOf<JsonObject>()
    var currentMap = mutableMapOf<String, JsonElement>()

    val onStep = { currentMap.clear() }
    val afterStep: () -> Unit = { currentList.add(JsonObject(currentMap.toMap())) }

    @OptIn(ExperimentalSerializationApi::class)
    fun build(block: Builder.() -> Unit): String {
        currentList.clear()
        val builder = Builder(onStep, afterStep)
        builder.block()

        val arr = JsonArray(currentList)

        val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
        return json.encodeToString(arr)
    }

    class Builder(val onStep: () -> Unit, val afterStep: () -> Unit) {

        fun step(block: () -> Unit) {
            onStep()
            block()
            afterStep()
        }
    }

    var lastModifier = null as String?
    infix fun <T> T.via(other: Easing) : T {
        lastModifier = other.ref
        return this
    }

    val onNumberSet = { propertyName: String, value: Number ->
        currentMap[propertyName] =
            lastModifier?.let{ buildJsonObject {
                put("value", value)
                put("easing", lastModifier)
                lastModifier = null
            } } ?: JsonPrimitive(value)
    }
    val onStringSet = { propertyName: String, value: String ->
        currentMap[propertyName] =
            lastModifier?.let{ buildJsonObject {
                put("value", value)
                put("easing", lastModifier)
                lastModifier = null
            } } ?: JsonPrimitive(value)
    }

    fun <T: Any> serialize(value: T): Memory<T> {
        return when (value) {
            is Number -> Memory(value, onNumberSet) as Memory<T>
            is String -> Memory(value, onStringSet) as Memory<T>
            is Easing -> Memory(value) { n, v -> onStringSet(n, v.ref) } as Memory<T>
            else -> throw IllegalArgumentException("Only Number and String are supported !")
        }
    }

    class Memory<T: Any>(var value: T, val onSet: (propertyName: String, value: T) -> Unit) {
        operator fun getValue(animationParser: AnimationParser, property: KProperty<*>): T {
            return value
        }

        /**
         * Only use during [step].
         */
        operator fun setValue(animationParser: AnimationParser, property: KProperty<*>, value: T) {
            onSet(property.name, value)
            this.value = value
        }
    }

    var time by serialize(0.0)
    var easing by serialize(Easing.LINEAR)

    ///////////////////////////////////////////////////////////////////

   fun buildAndWrite(targetFilename: String, block: Builder.() -> Unit)  {

       val output = build(block)

       var currentPath = System.getProperty("user.dir")
       val pathSegs = listOf("data", "keyframes")
       pathSegs.forEach {
           currentPath += File.separator + it
           val f = File(currentPath)
           if (f.exists().not()) {
               f.mkdir()
           }
       }
       currentPath += File.separator + targetFilename
       val f = File(currentPath)
       f.createNewFile()
       f.writeText(output)
   }


    /**
     * @param ref reference for json data
     */
    enum class Easing(val ref: String) {
        LINEAR("linear"),
        BACK_IN("back-in"),
        BACK_OUT("back-out"),
        BACK_IN_OUT("back-in-out"),
        BOUNCE_IN("bounce-in"),
        BOUNCE_OUT("bounce-out"),
        BOUNCE_IN_OUT("bounce-in-out"),
        CIRC_IN("circ-in"),
        CIRC_OUT("circ-out"),
        CIRC_IN_OUT("circ-in-out"),
        CUBIC_IN("cubic-in"),
        CUBIC_OUT("cubic-out"),
        CUBIC_IN_OUT("cubic-in-out"),
        ELASTIC_IN("elastic-in"),
        ELASTIC_OUT("elastic-out"),
        ELASTIC_IN_OUT("elastic-in-out"),
        EXPO_IN("expo-in"),
        EXPO_OUT("expo-out"),
        EXPO_IN_OUT("expo-in-out"),
        QUAD_IN("quad-in"),
        QUAD_OUT("quad-out"),
        QUAD_IN_OUT("quad-in-out"),
        QUART_IN("quart-in"),
        QUART_OUT("quart-out"),
        QUART_IN_OUT("quart-in-out"),
        QUINT_IN("quint-in"),
        QUINT_OUT("quint-out"),
        QUINT_IN_OUT("quint-in-out"),
        SINE_IN("sine-in"),
        SINE_OUT("sine-out"),
        SINE_IN_OUT("sine-in-out"),
        ONE("one"),
        ZERO("zero"),
        ;

        fun getByValue(value: String): Easing? {
           return Easing.values().find { this.ref == value }
        }
    }
}

