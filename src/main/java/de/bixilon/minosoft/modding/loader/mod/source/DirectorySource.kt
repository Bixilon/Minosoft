/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import com.github.ajalt.clikt.core.FileNotFound
import de.bixilon.kutil.stream.InputStreamUtil.readAll
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.modding.loader.LoaderUtil
import de.bixilon.minosoft.modding.loader.LoaderUtil.load
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.logging.LogOptions
import java.io.File
import java.io.FileInputStream

class DirectorySource(
    val directory: File,
) : ModSource, TextFormattable {

    init {
        if (!directory.isDirectory) throw FileNotFound("Not a directory: $directory")
    }

    override fun process(mod: MinosoftMod) {
        val files = directory.listFiles()!!
        val assets = DirectoryAssetsManager(directory.toPath())
        assets.load()

        for (sub in files) {
            if (sub.isDirectory && sub.name == "assets") {
                continue
            }
            if (sub.isFile && sub.name == LoaderUtil.MANIFEST) {
                mod.manifest = FileInputStream(sub).readJson()
                continue
            }
            scanClasses(mod, directory, sub)
        }

        mod.assetsManager = assets
    }

    override fun toString(): String {
        return "directory:$directory"
    }

    override fun toText(): Any {
        return BaseComponent("directory:", TextComponent(directory.path).clickEvent(OpenFileClickEvent(directory)))
    }

    companion object {

        fun scanClasses(mod: MinosoftMod, base: File, file: File) {
            if (file.isFile) {
                if (!file.name.endsWith(".class")) return

                val path = file.path.removePrefix(base.path).removePrefix(File.separator)
                if (LogOptions.verbose) {
                    Log.log(LogMessageType.MOD_LOADING, LogLevels.VERBOSE) { "Injecting class $path" }
                }
                mod.classLoader.load(path, FileInputStream(file).readAll())
            }
            if (file.isDirectory) {
                for (sub in file.listFiles()!!) {
                    scanClasses(mod, base, sub)
                }
            }
        }
    }
}
