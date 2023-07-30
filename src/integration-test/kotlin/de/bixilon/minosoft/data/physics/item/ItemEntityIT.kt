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

package de.bixilon.minosoft.data.physics.item

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.GlassTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.startInit
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class ItemEntityIT {

    private fun createItem(connection: PlayConnection): ItemEntity {
        connection.world.dimension::skyLight.forceSet(false)
        val entity = ItemEntity(connection, connection.registries.entityType[ItemEntity]!!, EntityData(connection), Vec3d.EMPTY, EntityRotation.EMPTY)
        entity.startInit()

        return entity
    }

    fun itemFalling1() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        entity.runTicks(10)
        entity.assertPosition(0.0, 2.9268649250204053, 0.0)
        entity.assertVelocity(0.0, -0.358537298500408, 0.0)

        entity.assertGround(false)
    }

    fun itemFalling2() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 5.0, 0.0))
        entity.runTicks(19)
        entity.assertPosition(0.0, -1.7607971755094471, 0.0)
        entity.assertVelocity(0.0, -0.6247840564898108, 0.0)

        entity.assertGround(false)
    }

    fun itemLanding1() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 6.0, 0.0))
        entity.connection.world[Vec3i(0, 4, 0)] = GlassTest0.state
        entity.runTicks(16)
        entity.assertPosition(0.0, 5.0, 0.0)
        // TODO entity.assertVelocity(0.0, -0.12, 0.0)
        entity.assertGround(true)
    }

    fun itemLanding2() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 6.0, 0.0))
        entity.connection.world[Vec3i(0, 4, 0)] = GlassTest0.state
        entity.runTicks(20)
        entity.assertPosition(0.0, 5.0, 0.0)
        entity.assertVelocity(0.0, -0.0, 0.0)
        entity.assertGround(true)
    }

    fun itemLanding3() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 6.0, 0.0))
        entity.connection.world[Vec3i(0, 4, 0)] = GlassTest0.state
        entity.runTicks(25)
        entity.assertPosition(0.0, 5.0, 0.0)
        // TODO entity.assertVelocity(0.0, -0.08, 0.0)
        entity.assertGround(true)
    }

    fun itemVelocityY() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 6.0, 0.0))
        entity.physics.velocity = Vec3d(0.0, 0.2, 0.0)
        entity.runTicks(10)
        entity.assertPosition(0.0, 5.756136856144935, 0.0)
        entity.assertVelocity(0.0, -0.1951227371228987, 0.0)
        entity.assertGround(false)
    }

    fun itemVelocity2() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 6.0, 0.0))
        entity.physics.velocity = Vec3d(0.1, 0.3, 0.1)
        entity.runTicks(10)
        entity.assertPosition(0.9146360427032717, 6.670772821707201, 0.9146360427032717)
        entity.assertVelocity(0.08170729659123263, -0.11341545643414405, 0.08170729659123263)
        entity.assertGround(false)
    }

    fun itemVelocity3() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 6.0, 0.0))
        entity.physics.velocity = Vec3d(0.1, 0.3, -0.1)
        entity.connection.world.fill(Vec3i(-5, 4, -5), Vec3i(5, 4, 5), GlassTest0.state)
        entity.runTicks(30)
        entity.assertPosition(1.55521462290592, 5.0, -1.55521462290592)
        entity.assertVelocity(6.081541491145816E-4, -0.04, -6.081541491145816E-4)
        entity.assertGround(true)
    }

    fun itemVelocity4() {
        val entity = createItem(createConnection(2))
        entity.forceTeleport(Vec3d(0.0, 6.0, 0.0))
        entity.physics.velocity = Vec3d(-0.5, -0.1, 0.8)
        entity.connection.world.fill(Vec3i(-5, 4, -5), Vec3i(5, 4, 5), GlassTest0.state)
        entity.runTicks(30)
        entity.assertPosition(-3.0597864176904332, 5.0, 4.895658268304694)
        entity.assertVelocity(-1.6015292949696051E-4, -0.04, 2.56244687195137E-4)
        entity.assertGround(true)
    }
}
