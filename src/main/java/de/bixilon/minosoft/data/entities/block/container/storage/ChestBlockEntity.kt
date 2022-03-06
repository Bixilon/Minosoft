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

package de.bixilon.minosoft.data.entities.block.container.storage

import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.ChestTypes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.DoubleChestRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.SingleChestRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3i

open class ChestBlockEntity(connection: PlayConnection) : StorageBlockEntity(connection) {

    override fun createRenderer(renderWindow: RenderWindow, blockState: BlockState, blockPosition: Vec3i): BlockEntityRenderer<*>? {
        val type = blockState.properties[BlockProperties.CHEST_TYPE] ?: return null
        if (type == ChestTypes.SINGLE) {
            return SingleChestRenderer(this, renderWindow, blockState, blockPosition, renderWindow.modelLoader.entities.models[getSingleModel()] ?: return null)
        }

        if (type == ChestTypes.LEFT) {
            // only left chest will be rendered (the model is the double chest)
            return DoubleChestRenderer(this, renderWindow, blockState, blockPosition, renderWindow.modelLoader.entities.models[getDoubleModel()] ?: return null)
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
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:chest")

        override fun build(connection: PlayConnection): ChestBlockEntity {
            return ChestBlockEntity(connection)
        }
    }
}
