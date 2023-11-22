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
import com.fasterxml.jackson.module.kotlin.convertValue
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.config.profile.Boxed
import de.bixilon.minosoft.util.json.Jackson
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["profiles"])
class ProfileManagerTest {
    private val jacksonType by lazy { Jackson.MAPPER.typeFactory.constructType(TestProfile::class.java) }
    private val reader by lazy { Jackson.MAPPER.readerFor(jacksonType) }

    private fun create(): TestProfile {
        return TestProfile()
    }

    private fun TestProfile.update(manager: TestProfileManager, data: JsonObject) {
        val tree = Jackson.MAPPER.convertValue<ObjectNode>(data)
        manager.unsafeUpdate(this, tree)
    }

    private fun TestProfile.serialize(): JsonObject {
        return Jackson.MAPPER.convertValue(this, Jackson.JSON_MAP_TYPE)
    }

    fun setup() {
        reader
        val profile = create()
    }

    fun `update with just the normal property`() {
        val profile = create()
        assertEquals(profile.config.normal, "test")
        profile.update(TestProfileManager(), mapOf("config" to mapOf("normal" to "abc")))
        assertEquals(profile.config.normal, "abc")
    }

    fun `update redirect property`() {
        val profile = create()
        assertEquals(profile.config.prop, null)

        profile.update(TestProfileManager(), mapOf("config" to mapOf("prop" to 12)))
        assertEquals(profile.config.prop, Boxed(12, false))
    }

    fun `assign redirect property`() {
        val profile = create()
        profile.config.prop = Boxed(123, true)
    }

    fun `serialize redirect property`() {
        val profile = create()
        profile.config.prop = Boxed(123, true)
        val data = profile.serialize()
        assertEquals(data, mapOf("config" to mapOf("prop" to 123, "normal" to "test"), "key" to 1))
    }
}
