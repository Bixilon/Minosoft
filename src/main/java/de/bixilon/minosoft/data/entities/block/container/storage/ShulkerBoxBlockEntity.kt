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

package de.bixilon.minosoft.data.entities.block.container.storage

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.colors.DyeColors
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.shulker.ShulkerBoxRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ShulkerBoxBlockEntity(connection: PlayConnection) : StorageBlockEntity(connection) {

    override fun createRenderer(context: RenderContext, state: BlockState, position: Vec3i, light: Int): BlockEntityRenderer<*>? {
        // TODO: remove that junk code
        val model: BakedSkeletalModel?
        val prefix = state.block.identifier.path.removeSuffix("shulker_box")
        if (prefix.endsWith("_")) {
            // colored
            val color = DyeColors[prefix.removeSuffix("_")]
            model = context.models.skeletal[ShulkerBoxRenderer.NAME_COLOR[color.ordinal]]
            // TODO: light gray -> silver (<1.13)
        } else {
            model = context.models.skeletal[ShulkerBoxRenderer.NAME]
        }
        if (model == null) return null
        return ShulkerBoxRenderer(this, context, state, position, model, light)
    }

    companion object : BlockEntityFactory<ShulkerBoxBlockEntity> {
        override val identifier: ResourceLocation = minecraft("shulker_box")

        override fun build(connection: PlayConnection): ShulkerBoxBlockEntity {
            return ShulkerBoxBlockEntity(connection)
        }
    }
}
