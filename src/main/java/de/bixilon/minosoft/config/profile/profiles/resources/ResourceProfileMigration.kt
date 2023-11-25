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
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.util.json.Jackson

object ResourceProfileMigration {

    /**
     * Pixlyzer and minosoft meta urls are now a list
     */
    fun migrate1(data: ObjectNode) {
        val source = data.get("source").nullCast<ObjectNode>() ?: return

        source.remove("pixlyzer")?.asText()?.let {
            if (it == "https://gitlab.bixilon.de/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false") return@let
            val array = Jackson.MAPPER.createArrayNode()
            array.add(it)
            source.replace("pixlyzer", array)
        }
        source.remove("minosoft-meta")?.asText()?.let {
            if (it == "https://gitlab.bixilon.de/bixilon/minosoft-meta-bin/-/raw/master/\${hashPrefix}/\${fullHash}?ref_type=heads") return@let
            val array = Jackson.MAPPER.createArrayNode()
            array.add(it)
            source.replace("minosoft-meta", array)
        }
    }
}
