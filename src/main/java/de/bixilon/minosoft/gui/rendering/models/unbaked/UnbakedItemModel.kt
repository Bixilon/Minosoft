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

package de.bixilon.minosoft.gui.rendering.models.unbaked

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.GUILights
import de.bixilon.minosoft.gui.rendering.models.baked.item.BakedItemModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil

class UnbakedItemModel(
    parent: GenericUnbakedModel?,
    json: Map<String, Any>,
) : GenericUnbakedModel(parent, json) {
    val guiLight: GUILights = json["gui_light"]?.toString()?.let { GUILights[it] } ?: parent?.let { return@let if (parent is UnbakedItemModel) parent.guiLight else null } ?: GUILights.SIDE

    // ToDo: Overrides (predicates)

    override fun bake(context: RenderContext): BakedItemModel {
        val textures = TextureUtil.resolveTextures(context.textureManager.staticTextures, textures)

        var itemTexture: Texture? = null
        textures.iterator().let {
            if (it.hasNext()) {
                itemTexture = it.next().value
            }
        }
        return BakedItemModel(itemTexture)
    }
}
