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

package de.bixilon.minosoft.config.profile.test

import com.fasterxml.jackson.databind.node.ObjectNode
import de.bixilon.minosoft.config.profile.storage.StorageProfileManager

class TestProfileManager : StorageProfileManager<TestProfile>() {
    override val type get() = TestProfile
    override val latestVersion get() = 2


    override fun migrate(version: Int, data: ObjectNode) = when (version) {
        1 -> migrate1(data)
        else -> Unit
    }

    fun migrate1(data: ObjectNode) {
        data.remove("key_old")?.asInt()?.let { data.put("key", it) }
    }
}
