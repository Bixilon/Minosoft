/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.loader.mod.source

import de.bixilon.minosoft.assets.file.ZipAssetsManager
import de.bixilon.minosoft.assets.util.FileUtil.readJson
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.modding.loader.LoaderUtil
import de.bixilon.minosoft.modding.loader.LoaderUtil.load
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

class ArchiveSource(
    val jar: File,
) : ModSource, TextFormattable {

    override fun process(mod: MinosoftMod) {
        val stream = JarInputStream(FileInputStream(jar))
        val assets = ZipAssetsManager(stream)
        val namespaces: MutableSet<String> = mutableSetOf()
        while (true) {
            val entry = stream.nextEntry ?: break
            if (entry.isDirectory) {
                continue
            }

            if (entry.name.endsWith(".class") && entry is JarEntry) {
                mod.classLoader.load(entry, stream)
            } else if (entry.name == LoaderUtil.MANIFEST) {
                mod.manifest = stream.readJson(false)
            } else {
                assets.push(entry.name, namespaces, stream)
            }
        }
        stream.close()
        assets.namespaces = namespaces
        mod.assetsManager = assets
        assets.loaded = true
    }

    override fun toString(): String {
        return "jar:$jar"
    }

    override fun toText(): Any {
        return BaseComponent("jar:", TextComponent(jar.path).clickEvent(OpenFileClickEvent(jar)))
    }
}
