package playground.keyframer

import java.io.File

enum class ResFiles(var filename: String) {
    BASIC_ANIM("demo-envelope-01.json"),
    REPEATED_ANIM("repeatedAnim.json"),
    CREATED_ANIM("persistedAnim.json")
    ;

    val path = listOf(
        System.getProperty("user.dir"), "data", "keyframes", filename
    ).joinToString(File.separator)
}
