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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.horse.AbstractHorse
import de.bixilon.minosoft.data.entities.entities.animal.horse.Horse
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class HorseRidingTest : AbstractRidingTest<Horse>() {


    override fun constructVehicle(connection: PlayConnection): Entity {
        val type = connection.registries.entityType[Horse]!!
        return Horse.build(connection, type, EntityData(connection), Vec3d.EMPTY, EntityRotation.EMPTY)
    }

    override fun saddle(entity: Horse) {
        entity.data[AbstractHorse.FLAGS_DATA] = 0x00.inv()
    }

    fun horseStartRiding() {
        val player = super.startRiding()

        assertNotNull(player.attachment.vehicle)
        assertNull(player.attachment.vehicle!!.primaryPassenger)
        assertEquals(player.attachment.vehicle!!.attachment.passengers, setOf(player))

        player.assertPosition(6.0, 5.0, 4.0)
        player.assertVelocity(0.0, 0.0, 0.0)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun horseFalling() {
        val player = super.falling()

        player.assertPosition(7.0, 3.733527198554499, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)

        player.attachment.vehicle!!.assertPosition(7.0, 2.8835271806731053, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, -0.6580691322055405, 0.0)
    }

    fun horseWalking1() {
        val player = super.walking1()

        player.assertPosition(7.0, 3.733527198554499, 5.753721497097093)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 2.8835271806731053, 5.753721497097093)
        player.attachment.vehicle!!.assertVelocity(0.0, -0.6580691322055405, 0.11928300888309169)
    }

    fun horseWalking2() {
        val player = super.walking2()

        player.assertPosition(7.0, 5.850000017881394, 9.181125855108673)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 5.0, 9.181125855108673)
        player.attachment.vehicle!!.assertVelocity(0.0, -0.0784000015258789, 0.2583138725090716)
        player.attachment.vehicle!!.assertGround()
    }

    fun horseWalkSideways1() {
        val player = super.walkSideways1()

        player.assertPosition(8.38288096570885, 5.850000017881394, 5.0)
        player.assertVelocity(0.017836000462502232, -0.0784000015258789, 0.0)

        player.attachment.vehicle!!.assertPosition(8.38288096570885, 5.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.1273869934409394, -0.0784000015258789, 0.0)
        player.attachment.vehicle!!.assertGround()
    }

    fun horseWalkSideways2() {
        val player = super.walkSideways2()

        player.assertPosition(9.124161152733638, 5.850000017881394, 9.248322305467276)
        player.assertVelocity(0.012869343500835182, -0.0784000015258789, 0.012869343500835182)

        player.attachment.vehicle!!.assertPosition(9.124161152733638, 5.0, 9.248322305467276)
        player.attachment.vehicle!!.assertVelocity(0.11801492695988425, -0.0784000015258789, 0.2360298539197685)
        player.attachment.vehicle!!.assertGround()
    }

    fun horseWalkUnsaddled() {
        val player = super.walkUnsaddled()

        player.assertPosition(7.0, 6.850000017881394, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 6.0, 5.0)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.0, 0.0)
    }

    fun horseJump1() {
        val player = super.jump1()
        player.assertPosition(7.0, 5.850000017881394, 11.076987398155309)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 5.0, 10.602808343386508)
        player.attachment.vehicle!!.assertVelocity(0.0, -0.0784000015258789, 0.2588561799168589)
        player.attachment.vehicle!!.assertGround()
    }

    fun horseJump2() {
        val player = super.jump2()

        player.assertPosition(7.0, 6.889699886918715, 11.981402219748995)
        player.assertVelocity(0.0, -0.0784000015258789, 0.017836000462502232)

        player.attachment.vehicle!!.assertPosition(7.0, 5.972199866355112, 12.29640221736481)
        player.attachment.vehicle!!.assertVelocity(0.0, 0.3499469691407101, 0.39986609131963496)
        player.attachment.vehicle!!.assertGround()
    }
}
