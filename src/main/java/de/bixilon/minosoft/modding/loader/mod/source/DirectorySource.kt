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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
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

class DirectorySource(
    val directory: File,
) : ModSource, TextFormattable {

    override fun process(mod: MinosoftMod) {
        val files = directory.listFiles()!!
        val assets = DirectoryAssetsManager(directory.path)
        assets.load(CountUpAndDownLatch(0))

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

        fun scanClasses(mod: MinosoftMod, baseDirectory: File, currentDirectory: File) {
            if (currentDirectory.isFile) {
                mod.classLoader.load(currentDirectory.path.removePrefix(baseDirectory.path).removePrefix("/"), FileInputStream(currentDirectory).readAllBytes())
            }
            if (currentDirectory.isDirectory) {
                for (sub in currentDirectory.listFiles()!!) {
                    scanClasses(mod, baseDirectory, sub)
                }
            }
        }
    }
}
