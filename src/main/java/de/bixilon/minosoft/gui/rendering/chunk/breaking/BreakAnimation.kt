/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.breaking

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture

class BreakAnimation(
    val interpolate: Boolean,
    val frames: Array<Texture>,
) {

    companion object {
        val ANIMATION = minecraft("rendering/block_break_animation.json")

        private class Prototype(
            val interpolate: Boolean = true,
            val frames: Array<ResourceLocation>
        )

        fun load(texture: TextureManager, assets: AssetsManager): BreakAnimation {
            val prototype = assets[ANIMATION].readJson<Prototype>()

            return BreakAnimation(prototype.interpolate, prototype.frames.map { texture.static.create(it.texture()) }.toTypedArray())
        }
    }
}
