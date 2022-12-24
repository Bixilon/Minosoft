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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.entities.MeshedBlockEntityRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class MeshedBlockEntity(connection: PlayConnection) : BlockEntity(connection) {

    override fun getRenderer(context: RenderContext, blockState: BlockState, blockPosition: Vec3i, light: Int): MeshedBlockEntityRenderer<*>? {
        var renderer = this.renderer
        if (renderer is MeshedBlockEntityRenderer<*> && renderer.blockState == blockState) {
            return renderer
        }
        renderer = createMeshedRenderer(context, blockState, blockPosition)
        this.renderer = renderer
        return renderer
    }

    override fun createRenderer(context: RenderContext, blockState: BlockState, blockPosition: Vec3i, light: Int): BlockEntityRenderer<*> {
        throw IllegalAccessException()
    }

    abstract fun createMeshedRenderer(context: RenderContext, blockState: BlockState, blockPosition: Vec3i): MeshedBlockEntityRenderer<*>
}
