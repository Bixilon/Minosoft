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

package de.bixilon.minosoft.tags

import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft


object MinecraftTagTypes {
    val BLOCK = minecraft("block")
    val ITEM = minecraft("item")
    val FLUID = minecraft("fluid")
    val ENTITY_TYPE = minecraft("entity_type")
    val GAME_EVENT = minecraft("game_event")
}
