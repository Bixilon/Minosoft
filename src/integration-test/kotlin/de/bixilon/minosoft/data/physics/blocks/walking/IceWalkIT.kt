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

package de.bixilon.minosoft.data.physics.blocks.walking

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.parkour.ParkourUtil
import de.bixilon.minosoft.data.physics.parkour.ParkourUtil.run
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class IceWalkIT : WalkIT() {

    @Test(priority = -1)
    fun getIce() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.ICE]?.states?.default ?: throw SkipException("Can not find ice!")
    }

    fun iceLanding() {
        val player = super.landing()

        player.assertPosition(6.0, 1.0, 6.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun iceWalking1() {
        val player = super.walking1()

        player.assertPosition(6.0, 1.0, 6.057036000349253)
        player.assertVelocity(0.0, -0.0784000015258789, 0.034066761351146994)
    }

    fun iceWalking2() {
        val player = super.walking2()

        player.assertPosition(6.0, 1.0, 6.8810065172563935)
        player.assertVelocity(0.0, -0.0784000015258789, 0.124838222076967)
    }

    fun iceWalking3() {
        val player = super.walking3()

        player.assertPosition(6.0, 1.0, 14.641755815396055)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18475066865558915)
    }

    fun iceWalking4() {
        val player = super.walking4()

        player.assertPosition(-6.0, 1.0, -5.1189934827436065)
        player.assertVelocity(0.0, -0.0784000015258789, 0.124838222076967)
    }

    fun iceWalking5() {
        val player = super.walking5()

        player.assertPosition(-6.0, 1.0, 2.641755815396057)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18475066865558915)
    }

    fun iceWalking6() {
        val player = super.walking6()

        player.assertPosition(-6.0, 1.0, -6.8810065172563935)
        player.assertVelocity(-2.456291834852546E-18, -0.0784000015258789, -0.124838222076967)
    }

    fun iceWalking7() {
        val player = super.walking7()

        player.assertPosition(-6.0, 1.0, -14.641755815396055)
        player.assertVelocity(-2.456291834852546E-18, -0.0784000015258789, -0.18475066865558915)
    }

    fun iceWalking8() {
        val player = super.walking8()

        player.assertPosition(-7.04985115382941, 1.0, -6.290667622047385)
        player.assertVelocity(0.11359384658572699, -0.0784000015258789, 0.036193427572131315)
    }

    fun iceWalking9() {
        val player = super.walking9()

        player.assertPosition(-19.35198652032009, 1.0, 2.6161220435229264)
        player.assertVelocity(-0.16640790187855525, -0.0784000015258789, -0.0744010079335596)
    }

    fun iceSprintJump1() {
        val player = super.sprintJump1()

        player.assertPosition(-6.0, 1.0, -5.937083998572499)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03941756248656188)
    }

    fun iceSprintJump2() {
        val player = super.sprintJump2()

        player.assertPosition(-6.0, 2.0013359791121474, -5.136670854551173)
        player.assertVelocity(0.0, 0.16477328182606651, 0.2426887021278192)
    }

    fun iceSlabWalk() {
        val player = super.slabWalk()

        player.assertPosition(-6.0, 1.5, -5.411464909381884)
        player.assertVelocity(0.0, -0.0784000015258789, 0.04403047150789242)
        player.assertGround()
    }

    fun iceStoneWalk1() {
        val player = super.stoneBlockWalk1()

        player.assertPosition(-15.651896342730515, 4.0, 7.396484707393364)
        player.assertVelocity(1.3499081921536198E-4, -0.0784000015258789, -0.16430408375534813)
        player.assertGround()
    }

    fun iceStoneWalk2() {
        val player = super.stoneBlockWalk2()

        player.assertPosition(-15.652653187383224, 4.0, 8.199920578169662)
        player.assertVelocity(1.3499081921536198E-4, -0.0784000015258789, -0.10504867215526327)
        player.assertGround()
    }


    @Test(enabled = false)
    fun iceParkour1() {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(-18, 3, 2), Vec3i(-10, 3, 10), StoneTest0.state)
        player.connection.world.fill(Vec3i(-16, 3, 4), Vec3i(-12, 3, 8), block!!)

        val ticks = ParkourUtil.read("ice_parkour_1")
        player.run(ticks)
    }

    @Test(enabled = false)
    fun iceParkourWalk1() {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(-18, 3, 2), Vec3i(-10, 3, 10), StoneTest0.state)
        player.connection.world.fill(Vec3i(-16, 3, 4), Vec3i(-12, 3, 8), block!!)

        val ticks = ParkourUtil.read("ice_walk_1")
        player.run(ticks)
    }
}
