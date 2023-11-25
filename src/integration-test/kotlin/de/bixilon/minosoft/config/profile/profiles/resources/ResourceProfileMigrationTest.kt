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

package de.bixilon.minosoft.config.profile.profiles.resources

import com.fasterxml.jackson.databind.node.ObjectNode
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.util.json.Jackson
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["profiles"])
class ResourceProfileMigrationTest {


    fun `m1 default urls`() {
        val data = mapOf(
            "source" to mapOf(
                "pixlyzer" to "https://gitlab.bixilon.de/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false",
                "minosoft-meta" to "https://gitlab.bixilon.de/bixilon/minosoft-meta-bin/-/raw/master/\${hashPrefix}/\${fullHash}?ref_type=heads",
            )
        )
        val tree = Jackson.MAPPER.valueToTree<ObjectNode>(data)
        ResourceProfileMigration.migrate1(tree)
        val map = Jackson.MAPPER.convertValue<JsonObject>(tree, Jackson.JSON_MAP_TYPE)

        assertEquals(map, mapOf(
            "source" to emptyMap<String, Any>() // removing them will put the default value up
        ))
    }

    fun `m1 custom urls`() {
        val data = mapOf(
            "source" to mapOf(
                "pixlyzer" to "https://custom.bixilon.de/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false",
                "minosoft-meta" to "https://custom.bixilon.de/bixilon/minosoft-meta-bin/-/raw/master/\${hashPrefix}/\${fullHash}?ref_type=heads",
            )
        )
        val tree = Jackson.MAPPER.valueToTree<ObjectNode>(data)
        ResourceProfileMigration.migrate1(tree)
        val map = Jackson.MAPPER.convertValue<JsonObject>(tree, Jackson.JSON_MAP_TYPE)

        assertEquals(map, mapOf(
            "source" to mapOf(
                "pixlyzer" to listOf(
                    "https://custom.bixilon.de/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false",
                ),
                "minosoft-meta" to listOf(
                    "https://custom.bixilon.de/bixilon/minosoft-meta-bin/-/raw/master/\${hashPrefix}/\${fullHash}?ref_type=heads",
                ),
            )
        ))
    }
}
