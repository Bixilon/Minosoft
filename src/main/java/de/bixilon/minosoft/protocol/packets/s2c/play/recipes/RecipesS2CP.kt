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

package de.bixilon.minosoft.protocol.packets.s2c.play.recipes

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W03A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.recipes.OtherRecipe
import de.bixilon.minosoft.recipes.Recipe
import de.bixilon.minosoft.recipes.RecipeFactories
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class RecipesS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val recipes: Map<ResourceLocation, Recipe>

    init {
        val recipes: MutableMap<ResourceLocation, Recipe> = mutableMapOf()
        for (i in 0 until buffer.readVarInt()) {
            val id: ResourceLocation
            val type: ResourceLocation
            if (buffer.versionId >= V_19W03A) {
                type = buffer.readResourceLocation()
                id = buffer.readResourceLocation()
            } else {
                id = buffer.readResourceLocation()
                type = buffer.readResourceLocation()
            }
            val factory = RecipeFactories[type]
            if (factory == null) {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Can not find recipe type $type" }
            }
            val recipe = factory?.build(buffer) ?: OtherRecipe(type)
            recipes[id] = recipe
        }
        this.recipes = recipes
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Recipes (count=${recipes.size})" }
        } else {
            Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "Recipes (recipes=$recipes)" }
        }
    }

    override fun handle(connection: PlayConnection) {
        for ((resourceLocation, recipe) in recipes) {
            connection.registries.recipes.add(null, resourceLocation, recipe)
        }
    }
}
