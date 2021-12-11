package de.bixilon.minosoft.assets.file

import de.bixilon.minosoft.assets.AssetsManager
import java.io.FileNotFoundException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


object ResourcesAssetsUtil {

    fun create(clazz: Class<*>): AssetsManager {
        val rootResources = clazz.classLoader.getResource("assets") ?: throw FileNotFoundException("Can not find assets folder in $clazz")

        return when (rootResources.protocol) {
            "file" -> DirectoryAssetsManager(rootResources.path)// Read them directly from the folder
            "jar" -> {
                val path: String = rootResources.path
                val jarPath = path.substring(5, path.indexOf("!"))
                val zip = URLDecoder.decode(jarPath, StandardCharsets.UTF_8)
                ZipAssetsManager(zip)
            }
            else -> TODO("Can not read resources: $rootResources")
        }
    }
}
