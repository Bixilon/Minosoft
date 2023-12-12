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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.util.KUtil.toResourceLocation

interface FileTexture : Texture {
    val resourceLocation: ResourceLocation

    override fun load(context: RenderContext) {
        load(context.connection.assetsManager)
    }

    fun load(assets: AssetsManager)


    companion object {

        fun AssetsManager.readImageProperties(texture: ResourceLocation): ImageProperties? {
            try {
                val stream = this.getOrNull("$texture.mcmeta".toResourceLocation()) ?: return null
                return stream.readJson(reader = ImageProperties.READER)
            } catch (error: Throwable) {
                error.printStackTrace()
            }
            return null
        }
    }
}
