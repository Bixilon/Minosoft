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

package de.bixilon.minosoft.data.physics.riding

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.item.items.fishing.rod.OnAStickItem
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class PigRidingTest : AbstractRidingTest<Pig>() {


    override fun constructVehicle(connection: PlayConnection): Entity {
        val type = connection.registries.entityType[Pig]!!
        return Pig.build(connection, type, EntityData(connection), Vec3d.EMPTY, EntityRotation.EMPTY)
    }

    override fun saddle(entity: Pig) {
        entity.data[Pig.SADDLED] = true
    }

    fun pigStartRiding() {
        val player = super.startRiding()

        assertNotNull(player.attachment.vehicle)
        assertNull(player.attachment.vehicle!!.primaryPassenger)
        assertEquals(player.attachment.vehicle!!.attachment.passengers, setOf(player))

        player.assertPosition(6.0, 5.0, 4.0)
        player.assertVelocity(0.0, 0.0, 0.0)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigFalling() {
        val player = super.falling()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigWalking1() {
        val player = super.walking1()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigWalking2() {
        val player = super.walking2()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigWalkSideways1() {
        val player = super.walkSideways1()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.017836000462502232, -0.0784000015258789, 0.0)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigWalkSideways2() {
        val player = super.walkSideways2()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.012869343500835182, -0.0784000015258789, 0.012869343500835182)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigWalkUnsaddled() {
        val player = super.walkUnsaddled()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigJump1() {
        val player = super.jump1()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun pigJump2() {
        val player = super.jump2()

        player.assertPosition(7.0, 6.324999982118607, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }


    private fun LocalPlayerEntity.addCarrotOnStick() {
        val stack = ItemStack(OnAStickItem.CarrotOnAStickItem())
        items.inventory[EquipmentSlots.MAIN_HAND] = stack
    }

    fun pigCarrotOnStick1() {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        assertNull(vehicle.primaryPassenger)
        player.addCarrotOnStick()
        assertEquals(vehicle.primaryPassenger, player)

        player.ridingTick(10)

        player.assertPosition(7.0, 3.208527162791712, 5.19227588837439)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)

        player.attachment.vehicle!!.assertPosition(7.0, 2.8835271806731053, 5.19227588837439)
        player.attachment.vehicle!!.assertVelocity(0.0, -0.6580691322055405, 0.03042933840855036)
    }

    fun pigCarrotOnStick2() {
        val player = createPlayer(createConnection(5))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        assertNull(vehicle.primaryPassenger)
        player.addCarrotOnStick()
        assertEquals(vehicle.primaryPassenger, player)

        player.ridingTick(20)


        player.assertPosition(7.0, 5.324999982118607, 6.671227302856487)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)

        player.attachment.vehicle!!.assertPosition(7.0, 5.0, 6.671227302856487)
        player.attachment.vehicle!!.assertVelocity(0.0, -0.0784000015258789, 0.06605260104847349)
        player.attachment.vehicle!!.assertGround()
    }
}
