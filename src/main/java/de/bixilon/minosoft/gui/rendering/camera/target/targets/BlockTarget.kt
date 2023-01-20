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

package de.bixilon.minosoft.gui.rendering.camera.target.targets

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.SimpleBlockState
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import java.util.*

open class BlockTarget(
    position: Vec3d,
    distance: Double,
    direction: Directions,
    val blockState: BlockState,
    val entity: BlockEntity?,
    val blockPosition: Vec3i,
) : GenericTarget(position, distance, direction), TextFormattable {
    val hitPosition = position - blockPosition

    override fun toString(): String {
        return toText().legacyText
    }

    override fun toText(): ChatComponent {
        val text = BaseComponent()

        text += "Block target "
        text += blockPosition
        text += ": "
        text += blockState.block.identifier

        text += "\n"

        if (blockState is SimpleBlockState) {
            text += blockState.withProperties()
        }

        return text
    }

    override fun hashCode(): Int {
        return Objects.hash(blockPosition, blockState, distance)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlockTarget) return false
        return distance == other.distance && blockPosition == other.blockPosition && blockState == other.blockState
    }
}
