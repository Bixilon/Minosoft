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
package de.bixilon.minosoft.protocol.packets.c2s.play.recipe.book

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_12_PRE6
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer

abstract class RecipeBookStateC2SP(
    val action: Action,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId < V_1_12_PRE6) {
            buffer.writeInt(action.ordinal)
        } else {
            buffer.writeVarInt(action.ordinal)
        }
    }

    enum class Action {
        DISPLAY_RECIPE,
        CRAFTING_BOOK_STATUS,
        ;

        companion object : ValuesEnum<Action> {
            override val VALUES: Array<Action> = values()
            override val NAME_MAP: Map<String, Action> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
