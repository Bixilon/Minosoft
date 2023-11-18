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

package de.bixilon.minosoft.data.world.entities

import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.observer.set.SetObserver.Companion.observeSet
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test
import java.util.*


@Test(groups = ["world"])
class WorldEntitiesTest {

    private fun create() = WorldEntities()
    private fun entity(): Entity {
        return Pig::class.java.allocate()
    }

    private fun player(): Entity {
        return LocalPlayerEntity::class.java.allocate()
    }

    fun setup() {
        create()
    }

    fun `properly observe`() {
        val entities = create()
        var notified = 0
        entities::entities.observeSet(this) { notified++ }
        val entity = entity()
        entities.add(12, UUID(0L, 0L), entity)
        assertEquals(notified, 1)
    }

    fun `id adding and removing`() {
        val entities = create()
        val entity = entity()
        entities.add(12, null, entity)
        assertEquals(entities.getId(entity), 12)
        entities.remove(entity)
        assertEquals(entities.getId(entity), null)
    }

    fun `uuid adding and removing`() {
        val entities = create()
        val entity = entity()
        entities.add(null, UUID(1, 1), entity)
        assertEquals(entities.getUUID(entity), UUID(1, 1))
        entities.remove(entity)
        assertEquals(entities.getUUID(entity), null)
    }

    fun `forbid removing local player`() {
        val entities = create()
        val player = player()
        entities.add(2, UUID(1, 2), player)
        entities.remove(player)
        assertNull(entities.getId(player))
        assertNull(entities.getUUID(player))
        assertTrue(player in entities.entities)
    }

    fun `clear with local player`() {
        val entities = create()
        val entity = entity()
        val player = player()

        entities::entities.observeSet(this) { if (player in it.removes) Broken() }
        entities.add(1, UUID(1, 1), entity)
        entities.add(2, UUID(1, 2), player)
        entities.clear(PlayConnection::class.java.allocate().apply { this::player.forceSet(player) }, true)

        assertNull(entities.getId(player))
        assertNull(entities.getId(entity))
        assertNull(entities.getUUID(player))
        assertNull(entities.getUUID(entity))
        assertTrue(player in entities.entities)
        assertFalse(entity in entities.entities)
    }

    fun `clear but not local player`() {
        val entities = create()
        val entity = entity()
        val player = player()

        entities::entities.observeSet(this) { if (player in it.removes) Broken() }
        entities.add(1, UUID(1, 1), entity)
        entities.add(2, UUID(1, 2), player)
        entities.clear(PlayConnection::class.java.allocate().apply { this::player.forceSet(player) }, false)

        assertEquals(entities.getId(player), 2)
        assertNull(entities.getId(entity))
        assertEquals(entities.getUUID(player), UUID(1, 2))
        assertNull(entities.getUUID(entity))
        assertTrue(player in entities.entities)
        assertFalse(entity in entities.entities)
    }
}
