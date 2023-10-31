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
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.ChestTypes
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.RenderedBlockEntity
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.chest.ChestRenderer
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.chest.DoubleChestRenderer
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.chest.SingleChestRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class ChestBlockEntity(connection: PlayConnection) : StorageBlockEntity(connection), RenderedBlockEntity<ChestRenderer> {
    override var renderer: ChestRenderer? = null

    override fun createRenderer(context: RenderContext, state: BlockState, position: Vec3i, light: Int): ChestRenderer? {
        val type: ChestTypes = state[BlockProperties.CHEST_TYPE]
        if (type == ChestTypes.SINGLE) {
            return SingleChestRenderer(this, context, state, position, context.models.skeletal[getSingleModel()] ?: return null, light)
        }

        if (type == ChestTypes.LEFT) {
            // only left chest will be rendered (the model is the double chest), reduces drawing overhead
            return DoubleChestRenderer(this, context, state, position, context.models.skeletal[getDoubleModel()] ?: return null, light)
        }

        return null
    }

    protected open fun getSingleModel(): ResourceLocation {
        return SingleChestRenderer.NormalChest.NAME
    }

    protected open fun getDoubleModel(): ResourceLocation {
        return DoubleChestRenderer.NormalChest.NAME
    }

    override fun onOpen() {
        super.onOpen()
        renderer?.open()
    }

    override fun onClose() {
        super.onClose()
        renderer?.close()
    }

    companion object : BlockEntityFactory<ChestBlockEntity> {
        override val identifier: ResourceLocation = minecraft("chest")

        override fun build(connection: PlayConnection): ChestBlockEntity {
            return ChestBlockEntity(connection)
        }
    }
}
