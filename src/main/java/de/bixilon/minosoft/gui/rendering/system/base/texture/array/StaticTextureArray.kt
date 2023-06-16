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

package de.bixilon.minosoft.gui.rendering.system.base.texture.array

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.sprite.SpriteAnimator
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file.PNGTexture

interface StaticTextureArray : TextureArray {
    val animator: SpriteAnimator
    val state: TextureArrayStates

    fun get(resourceLocation: ResourceLocation): Texture?

    fun pushTexture(texture: Texture)
    fun createTexture(resourceLocation: ResourceLocation, mipmaps: Boolean = true, properties: Boolean = true, default: (mipmaps: Boolean) -> Texture = { PNGTexture(resourceLocation, mipmaps = it) }): Texture

    fun preLoad(latch: AbstractLatch)
}
