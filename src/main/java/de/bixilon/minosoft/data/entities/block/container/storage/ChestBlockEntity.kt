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
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.ChestTypes
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.DoubleChestRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.SingleChestRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

open class ChestBlockEntity(connection: PlayConnection) : StorageBlockEntity(connection) {

    override fun createRenderer(context: RenderContext, blockState: BlockState, blockPosition: Vec3i, light: Int): BlockEntityRenderer<*>? {
        val type = blockState.properties[BlockProperties.CHEST_TYPE] ?: return null
        if (type == ChestTypes.SINGLE) {
            return SingleChestRenderer(this, context, blockState, blockPosition, context.modelLoader.entities.skeletal[getSingleModel()] ?: return null, light)
        }

        if (type == ChestTypes.LEFT) {
            // only left chest will be rendered (the model is the double chest), reduces drawing overhead
            return DoubleChestRenderer(this, context, blockState, blockPosition, context.modelLoader.entities.skeletal[getDoubleModel()] ?: return null, light)
        }

        return null
    }

    protected open fun getSingleModel(): ResourceLocation {
        return SingleChestRenderer.NormalChest.MODEL
    }

    protected open fun getDoubleModel(): ResourceLocation {
        return DoubleChestRenderer.NormalChest.MODEL
    }

    companion object : BlockEntityFactory<ChestBlockEntity> {
        override val identifier: ResourceLocation = KUtil.minecraft("chest")

        override fun build(connection: PlayConnection): ChestBlockEntity {
            return ChestBlockEntity(connection)
        }
    }
}
