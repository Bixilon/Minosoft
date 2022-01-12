/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.baked.item

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import glm_.vec2.Vec2i

@Deprecated("ToDo")
class BakedItemModel(
    val texture: AbstractTexture?,
) : BakedModel {

    fun render2d(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2i, itemStack: ItemStack) {
        val texture = texture ?: return
        consumer.addQuad(offset, offset + size, z, texture, tint = ChatColors.WHITE, options = options)
    }
}
