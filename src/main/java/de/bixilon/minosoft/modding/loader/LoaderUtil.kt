/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.loader

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.minosoft.assets.util.InputStreamUtil.readAll
import org.xeustechnologies.jcl.JarClassLoader
import org.xeustechnologies.jcl.JarResources
import org.xeustechnologies.jcl.JclJarEntry
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

object LoaderUtil {
    const val MANIFEST = "manifest.json"
    private val contentsField = JarResources::class.java.getFieldOrNull("jarEntryContents")!!
    private val classpathResourcesField = JarClassLoader::class.java.getFieldOrNull("classpathResources")!!


    val JarResources.contents: MutableMap<String, JclJarEntry>
        get() = contentsField.get(this).unsafeCast()


    val JarClassLoader.classpathResources: JarResources
        get() = classpathResourcesField.get(this).unsafeCast()

    fun JarClassLoader.load(entry: JarEntry, stream: JarInputStream) {
        load(entry.name, stream.readAll(false))
    }

    fun JarClassLoader.load(name: String, data: ByteArray) {
        val fixed = name.replace('\\', '/').replace(" ", "").replace(":", "") // class loaders just work with /
        val content = this.classpathResources.contents
        val entry = JclJarEntry()
        entry.baseUrl = null
        entry.resourceBytes = data
        content[fixed] = entry
    }

    fun JarClassLoader.unloadAll() {
        val content = this.classpathResources.contents.keys
        val removing = ArrayList<String>(content.size)
        for (name in content) {
            if (name.startsWith("assets/") || !name.endsWith(".class")) {
                continue
            }
            removing += name.removeSuffix(".class")
        }
        for (name in removing) {
            this.unloadClass(name)
        }
    }
}
