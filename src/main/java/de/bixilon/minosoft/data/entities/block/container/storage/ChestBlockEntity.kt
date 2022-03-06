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

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.ChestBlockEntityRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3i

open class ChestBlockEntity(connection: PlayConnection) : StorageBlockEntity(connection) {

    override fun createRenderer(renderWindow: RenderWindow, blockState: BlockState, blockPosition: Vec3i): BlockEntityRenderer<out BlockEntity>? {
        return ChestBlockEntityRenderer(this, renderWindow, blockState, blockPosition)
    }

    companion object : BlockEntityFactory<ChestBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:chest")
        val SINGLE_MODEL = "minecraft:block/entities/single_chest".toResourceLocation()
        val DOUBLE_MODEL = "minecraft:block/entities/double_chest".toResourceLocation()

        override fun build(connection: PlayConnection): ChestBlockEntity {
            return ChestBlockEntity(connection)
        }
    }
}
