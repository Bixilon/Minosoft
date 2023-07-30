/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kutil.latch.ParentLatch
import de.bixilon.minosoft.assets.directory.DirectoryAssetsManager
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.modding.loader.LoaderUtil
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import java.io.File
import java.io.FileInputStream

class SplitDirectorySource(
    val classes: File,
    val resources: File,
) : ModSource, TextFormattable {

    override fun process(mod: MinosoftMod) {
        processResources(mod)

        val classes = classes.listFiles()!!

        for (sub in classes) {
            DirectorySource.scanClasses(mod, this.classes, sub)
        }
    }

    override fun toString(): String {
        return "directory:$classes | $resources"
    }

    override fun toText(): Any {
        return BaseComponent("directory:", TextComponent(classes.path).clickEvent(OpenFileClickEvent(classes)), " | ", TextComponent(resources.path).clickEvent(OpenFileClickEvent(resources)))
    }

    private fun processResources(mod: MinosoftMod) {
        val assets = DirectoryAssetsManager(resources.path)
        assets.load(ParentLatch(0, mod.latch))
        mod.assetsManager = assets

        val manifestPath = File(resources.path + "/" + LoaderUtil.MANIFEST)
        mod.manifest = FileInputStream(manifestPath).readJson()
    }
}
