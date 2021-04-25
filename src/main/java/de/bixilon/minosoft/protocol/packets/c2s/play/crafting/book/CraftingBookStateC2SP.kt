/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.c2s.play.crafting.book

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_12_PRE6
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

abstract class CraftingBookStateC2SP(
    val action: CraftingBookStates,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId < V_1_12_PRE6) {
            buffer.writeInt(action.ordinal)
        } else {
            buffer.writeVarInt(action.ordinal)
        }
    }

    enum class CraftingBookStates {
        DISPLAY_RECIPE,
        CRAFTING_BOOK_STATUS,
        ;

        companion object : ValuesEnum<CraftingBookStates> {
            override val VALUES: Array<CraftingBookStates> = values()
            override val NAME_MAP: Map<String, CraftingBookStates> = KUtil.getEnumValues(VALUES)
        }
    }
}
