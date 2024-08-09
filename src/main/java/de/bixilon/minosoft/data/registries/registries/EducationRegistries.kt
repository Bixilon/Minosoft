/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.registries

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.protocol.versions.Version

object EducationRegistries {
    private val BLOCKS = minosoft("education/blocks.json")
    private val ENTITIES = minosoft("education/entities.json")
    private val ITEMS = minosoft("education/items.json")

    fun loadRegistry(version: Version, latch: AbstractLatch): Registries {
        val registries = Registries()

        val blocks: JsonObject = IntegratedAssets.DEFAULT[BLOCKS].readJsonObject()
        val entities: JsonObject = IntegratedAssets.DEFAULT[ENTITIES].readJsonObject()
        val items: JsonObject = IntegratedAssets.DEFAULT[ITEMS].readJsonObject()

        val data = mapOf(
            "blocks" to blocks,
            "entities" to entities,
            "items" to items,
        )

        registries.load(version, data, latch)

        return registries
    }
}
