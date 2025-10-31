/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.ShulkerBoxBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.DyedBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.shulker.ShulkerBoxRenderer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class ShulkerBoxBlockEntity(session: PlaySession, position: BlockPosition, state: BlockState) : StorageBlockEntity(session, position, state) {
    private var renderer: ShulkerBoxRenderer? = null

    override fun update(state: BlockState) {
        assert(state.block is ShulkerBoxBlock)
        super.update(state)
    }

    override fun createRenderer(context: RenderContext, light: Int): BlockEntityRenderer? {
        val block = state.block
        val name = when {
            block is DyedBlock -> ShulkerBoxRenderer.NAME_COLOR[block.color.ordinal]
            else -> ShulkerBoxRenderer.NAME
        }
        val model = context.models.skeletal[name] ?: return null
        this.renderer = ShulkerBoxRenderer(this, context, state, position, model, light)

        return this.renderer
    }

    override fun onOpen() {
        super.onOpen()
        renderer?.open()
    }

    override fun onClose() {
        super.onClose()
        renderer?.close()
    }

    companion object : BlockEntityFactory<ShulkerBoxBlockEntity> {
        override val identifier = minecraft("shulker_box")

        override fun build(session: PlaySession, position: BlockPosition, state: BlockState) = ShulkerBoxBlockEntity(session, position, state)
    }
}
