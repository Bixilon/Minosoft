/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.entities

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import org.testng.Assert.assertEquals
import org.testng.Assert.assertSame
import org.testng.annotations.Test

@Test(groups = ["entities"])
class EntityTest {

    private fun create() = TestEntity().apply { init() }

    // < 1.13
    fun `set string as custom name`() {
        val player = create()
        player.data[Entity.CUSTOM_NAME_DATA] = "Test"
        assertEquals(player.customName, TextComponent("Test"))
    }

    fun `set text component as custom name`() {
        val player = create()
        val name = TextComponent("Test2").color(ChatColors.RED)
        player.data[Entity.CUSTOM_NAME_DATA] = name
        assertSame(player.customName, name)
    }


    private class TestEntity(session: PlaySession = createSession()) : Entity(session, EntityType(Companion.identifier, null, 1.0f, 1.0f, factory = Companion), EntityData(session), Vec3d.EMPTY, EntityRotation.EMPTY) {


        companion object : EntityFactory<TestEntity> {
            override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) = Broken()
            override val identifier = minosoft("test")
        }
    }
}
