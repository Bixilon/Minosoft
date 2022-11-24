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
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

abstract class BlockEntity(
    val connection: PlayConnection,
) {
    protected open var renderer: BlockEntityRenderer<out BlockEntity>? = null
    open val nbt: JsonObject = emptyMap()

    open fun updateNBT(nbt: JsonObject) = Unit

    open fun tick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) = Unit

    open fun getRenderer(renderWindow: RenderWindow, blockState: BlockState, blockPosition: Vec3i, light: Int): BlockEntityRenderer<out BlockEntity>? {
        if (this.renderer?.blockState != blockState) {
            this.renderer = createRenderer(renderWindow, blockState, blockPosition, light)
        } else {
            this.renderer?.light = light
        }
        return this.renderer
    }

    protected open fun createRenderer(renderWindow: RenderWindow, blockState: BlockState, blockPosition: Vec3i, light: Int): BlockEntityRenderer<out BlockEntity>? = null
}
