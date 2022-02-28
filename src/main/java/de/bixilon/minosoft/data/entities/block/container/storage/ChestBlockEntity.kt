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
import de.bixilon.minosoft.gui.rendering.world.entities.renderer.storage.ChestBlockEntityRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ChestBlockEntity(connection: PlayConnection) : StorageBlockEntity(connection) {

    override fun createModel(): ChestBlockEntityRenderer {
        val model = ChestBlockEntityRenderer(this)
        this.model = model
        return model
    }

    companion object : BlockEntityFactory<ChestBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:chest")

        override fun build(connection: PlayConnection): ChestBlockEntity {
            return ChestBlockEntity(connection)
        }
    }
}
