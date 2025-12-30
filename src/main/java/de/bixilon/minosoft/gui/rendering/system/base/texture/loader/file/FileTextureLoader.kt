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

package de.bixilon.minosoft.gui.rendering.system.base.texture.loader.file

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.loader.TextureLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.loader.TextureLoaderResult
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.util.KUtil.toResourceLocation

abstract class FileTextureLoader(val file: ResourceLocation) : TextureLoader {

    protected abstract fun read(assets: AssetsManager): TextureBuffer

    override fun load(context: RenderContext): TextureLoaderResult {
        val assets = context.session.assets
        val buffer = read(assets)
        val properties = assets.readImageProperties(file)

        return TextureLoaderResult(buffer, properties)
    }


    override fun toString() = file.toString()

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
