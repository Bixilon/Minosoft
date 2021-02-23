/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.particle

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.IdentifierDeserializer
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.mappings.RegistryItem
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

data class Particle(
    val identifier: ModIdentifier,
    // ToDo
) : RegistryItem {
    override fun toString(): String {
        return identifier.toString()
    }

    companion object : IdentifierDeserializer<Particle> {
        override fun deserialize(mappings: VersionMapping, identifier: ModIdentifier, data: JsonObject): Particle {
            return Particle(identifier)
        }

    }
}
