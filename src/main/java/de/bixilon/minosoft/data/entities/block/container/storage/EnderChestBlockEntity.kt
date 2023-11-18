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
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.RenderedBlockEntity
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.chest.SingleChestRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class EnderChestBlockEntity(connection: PlayConnection) : StorageBlockEntity(connection), RenderedBlockEntity<SingleChestRenderer> {
    override var renderer: SingleChestRenderer? = null

    override fun createRenderer(context: RenderContext, state: BlockState, position: Vec3i, light: Int): SingleChestRenderer? {
        val model = context.models.skeletal[SingleChestRenderer.EnderChest.NAME] ?: return null
        return SingleChestRenderer(this, context, state, position, model, light)
    }

    override fun onOpen() {
        super.onOpen()
        renderer?.open()
    }

    override fun onClose() {
        super.onClose()
        renderer?.close()
    }

    companion object : BlockEntityFactory<EnderChestBlockEntity> {
        override val identifier: ResourceLocation = minecraft("ender_chest")

        override fun build(connection: PlayConnection): EnderChestBlockEntity {
            return EnderChestBlockEntity(connection)
        }
    }
}
