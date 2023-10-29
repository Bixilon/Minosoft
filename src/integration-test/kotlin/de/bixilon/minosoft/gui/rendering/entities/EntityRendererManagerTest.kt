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

package de.bixilon.minosoft.gui.rendering.entities

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.concurrent.queue.Queue
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.test.IT
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["entity_renderer", "rendering"])
class EntityRendererManagerTest {
    private val pig = EntityType(Pig.identifier, Namespaces.minecraft(""), 1.0f, 1.0f, true, false, mapOf(), Pig, null)


    private fun create(): EntityRendererManager {
        val connection = createConnection()
        val renderer = IT.OBJENESIS.newInstance(EntitiesRenderer::class.java)
        renderer::queue.forceSet(Queue())
        renderer::connection.forceSet(connection)
        return EntityRendererManager(renderer)
    }

    private fun EntityRendererManager.createPig(): Pig {
        return Pig(renderer.connection, this@EntityRendererManagerTest.pig, EntityData(renderer.connection, Int2ObjectOpenHashMap()), Vec3d.EMPTY, EntityRotation.EMPTY)
    }

    fun setup() {
        val renderer = create()
        renderer.init()
    }

    fun `spawn single entity`() {
        val renderer = create()
        renderer.init()
        val entity = renderer.createPig()
        assertEquals(renderer.size, 0)
        renderer.renderer.connection.world.entities.add(1, null, entity)
        renderer.renderer.queue.work()
        assertEquals(renderer.size, 1)
        renderer.renderer.connection.world.entities.remove(1)
        renderer.renderer.queue.work()
        assertEquals(renderer.size, 0)
    }

    fun `spawn multiple entities`() {
        val renderer = create()
        renderer.init()
        val e1 = renderer.createPig()
        val e2 = renderer.createPig()
        val e3 = renderer.createPig()
        assertEquals(renderer.size, 0)
        renderer.renderer.connection.world.entities.add(1, null, e1)
        renderer.renderer.connection.world.entities.add(2, null, e2)
        renderer.renderer.queue.work()
        assertEquals(renderer.size, 2)
        renderer.renderer.connection.world.entities.add(3, null, e3)
        renderer.renderer.queue.work()
        assertEquals(renderer.size, 3)
        renderer.renderer.connection.world.entities.remove(1)
        renderer.renderer.queue.work()
        assertEquals(renderer.size, 2)
        renderer.renderer.connection.world.entities.remove(2)
        renderer.renderer.connection.world.entities.remove(3)
        renderer.renderer.queue.work()
        assertEquals(renderer.size, 0)
    }
}
