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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.skeletal

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance

abstract class SkeletalBlockEntityRenderer<E : BlockEntity>(
    override var state: BlockState,
    protected val skeletal: SkeletalInstance?,
) : BlockEntityRenderer<E> {
    override var light = 0xFF

    override fun draw(context: RenderContext) {
        skeletal?.update()
        skeletal?.draw(light)
    }

    override fun load() {
        skeletal?.load()
    }

    override fun unload() {
        skeletal?.unload()
    }

    override fun drop() {
        skeletal?.drop()
    }
}
