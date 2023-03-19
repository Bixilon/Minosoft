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

package de.bixilon.minosoft.commands.parser.selector

class SelectorTarget<T>(
    val properties: Map<String, TargetProperty<T>>
) : AbstractTarget<T> {

    override fun filter(entries: Collection<T>): List<T> {
        val output: MutableList<T> = mutableListOf()
        entityLoop@ for (connection in entries) {
            for (property in properties.values) {
                if (!property.passes(connection)) {
                    continue@entityLoop
                }
            }
            output += connection
        }

        return output
    }
}
