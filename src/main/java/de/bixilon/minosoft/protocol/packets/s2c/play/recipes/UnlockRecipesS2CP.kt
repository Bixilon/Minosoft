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

package de.bixilon.minosoft.protocol.packets.s2c.play.recipes

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W48A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_12
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W27A
import de.bixilon.minosoft.recipes.Recipe
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class UnlockRecipesS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val action = Actions[if (buffer.versionId < V_1_12) buffer.readInt() else buffer.readVarInt()]

    val crafting = RecipeBookState(buffer)
    val smelting: RecipeBookState? = if (buffer.versionId >= V_17W48A) RecipeBookState(buffer) else null
    val blasting: RecipeBookState? = if (buffer.versionId >= V_20W27A) RecipeBookState(buffer) else null
    val smoking: RecipeBookState? = if (buffer.versionId >= V_20W27A) RecipeBookState(buffer) else null


    val listed: List<Recipe>

    init {
        val listed: MutableList<Recipe> = mutableListOf()
        val recipes = buffer.connection.registries.recipes
        for (i in 0 until buffer.readVarInt()) {
            listed += recipes[if (buffer.versionId < V_17W48A) buffer.readVarInt() else buffer.readResourceLocation()] ?: continue
        }
        this.listed = listed
    }

    val tagged: List<Recipe>

    init {
        val tagged: MutableList<Recipe> = mutableListOf()
        val recipes = buffer.connection.registries.recipes
        for (i in 0 until buffer.readVarInt()) {
            tagged += recipes[if (buffer.versionId < V_17W48A) buffer.readVarInt() else buffer.readResourceLocation()] ?: continue
        }
        this.tagged = tagged
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Recipes (action=$action, crafting=$crafting, smelting=$smelting, blasting=$blasting, smoking=$smoking, listed=$listed, tagged=$tagged)" }
    }

    enum class Actions {
        INITIALIZE,
        ADD,
        REMOVE,
        ;

        companion object : ValuesEnum<Actions> {
            override val VALUES: Array<Actions> = values()
            override val NAME_MAP: Map<String, Actions> = EnumUtil.getEnumValues(VALUES)
        }
    }

    class RecipeBookState(
        val bookOpen: Boolean,
        val filtering: Boolean,
    ) {
        constructor(buffer: PlayInByteBuffer) : this(buffer.readBoolean(), buffer.readBoolean())
    }
}

