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

package de.bixilon.minosoft.config.profile.delegate.types

import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.util.json.Jackson
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["profiles"])
class RedirectDelegateTest {
    private val jacksonType by lazy { Jackson.MAPPER.typeFactory.constructType(TestProfile::class.java) }
    private val reader by lazy { Jackson.MAPPER.readerFor(jacksonType) }

    private fun create(): TestProfile {
        return TestProfile()
    }

    private fun TestProfile.update(data: JsonObject) {
        val tree = Jackson.MAPPER.convertValue<ObjectNode>(data)


        val injectable = InjectableValues.Std()
        injectable.addValue(TestProfile::class.java, this)
        reader
            .withValueToUpdate(this)
            .with(injectable)
            .readValue<TestProfile>(tree)
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
        profile.update(mapOf("config" to mapOf("normal" to "abc")))
        assertEquals(profile.config.normal, "abc")
    }

    fun `update redirect property`() {
        val profile = create()
        assertEquals(profile.config.prop, null)
        profile.update(mapOf("config" to mapOf("normal" to 12)))
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
        assertEquals(data, mapOf("config" to mapOf("prop" to 123, "normal" to "test")))
    }


    class TestProfile(
        override var storage: ProfileStorage? = null,
        override val lock: Lock = ProfileLock(),
    ) : Profile {
        val config = TestConfig(this)
    }

    class TestConfig(profile: Profile) {
        var prop by RedirectDelegate<Boxed?, Int?>(profile, { it?.value }, { it?.let { Boxed(it, false) } })
        var normal by StringDelegate(profile, "test")
    }

    class Boxed(val value: Int, val unused: Boolean) {

        override fun equals(other: Any?): Boolean {
            if (other !is Boxed) return false
            return other.value == this.value
        }
    }
}
