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

package de.bixilon.minosoft.config.profile.profiles.account

import com.fasterxml.jackson.databind.node.ObjectNode
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.util.KUtil.toMap
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object AccountProfileMigration {
    private val MOJANG_ACCOUNT = minosoft("mojang_account")

    fun migrate1(data: ObjectNode) {
        val entries = data["entries"]?.nullCast<ObjectNode>()?.toMap() ?: return
        val remove: MutableSet<String> = mutableSetOf()
        for ((id, entry) in entries) {
            if (entry.get("type").asText().toResourceLocation() != MOJANG_ACCOUNT) {
                continue
            }
            remove += id
        }
        entries -= remove
    }
}
