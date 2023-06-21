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

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class PackedIceWalkIT : WalkIT() {

    @Test(priority = -1)
    fun getPackedIce() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.PACKED_ICE]?.states?.default ?: throw SkipException("Can not find packed packedIce!")
    }

    fun packedIceLanding() {
        val player = super.landing()

        player.assertPosition(6.0, 1.0, 6.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun packedIceWalking1() {
        val player = super.walking1()

        player.assertPosition(6.0, 1.0, 6.057036000349253)
        player.assertVelocity(0.0, -0.0784000015258789, 0.034066761351146994)
    }

    fun packedIceWalking2() {
        val player = super.walking2()

        player.assertPosition(6.0, 1.0, 6.8810065172563935)
        player.assertVelocity(0.0, -0.0784000015258789, 0.124838222076967)
    }

    fun packedIceWalking3() {
        val player = super.walking3()

        player.assertPosition(6.0, 1.0, 14.641755815396055)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18475066865558915)
    }

    fun packedIceWalking4() {
        val player = super.walking4()

        player.assertPosition(-6.0, 1.0, -5.1189934827436065)
        player.assertVelocity(0.0, -0.0784000015258789, 0.124838222076967)
    }

    fun packedIceWalking5() {
        val player = super.walking5()

        player.assertPosition(-6.0, 1.0, 2.641755815396057)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18475066865558915)
    }

    fun packedIceWalking6() {
        val player = super.walking6()

        player.assertPosition(-6.0, 1.0, -6.8810065172563935)
        player.assertVelocity(-2.456291834852546E-18, -0.0784000015258789, -0.124838222076967)
    }

    fun packedIceWalking7() {
        val player = super.walking7()

        player.assertPosition(-6.0, 1.0, -14.641755815396055)
        player.assertVelocity(-2.456291834852546E-18, -0.0784000015258789, -0.18475066865558915)
    }

    fun packedIceWalking8() {
        val player = super.walking8()

        player.assertPosition(-7.04985115382941, 1.0, -6.290667622047385)
        player.assertVelocity(0.11359384658572699, -0.0784000015258789, 0.036193427572131315)
    }

    fun packedIceWalking9() {
        val player = super.walking9()

        player.assertPosition(-19.35198652032009, 1.0, 2.6161220435229264)
        player.assertVelocity(-0.16640790187855525, -0.0784000015258789, -0.0744010079335596)
    }

    fun packedIceSprintJump1() {
        val player = super.sprintJump1()

        player.assertPosition(-6.0, 1.0, -5.937083998572499)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03941756248656188)
    }

    fun packedIceSprintJump2() {
        val player = super.sprintJump2()

        player.assertPosition(-6.0, 2.0013359791121474, -5.136670854551173)
        player.assertVelocity(0.0, 0.16477328182606651, 0.2426887021278192)
    }

    fun packedIceSlabWalk() {
        val player = super.slabWalk()

        player.assertPosition(-6.0, 1.5, -5.411464909381884)
        player.assertVelocity(0.0, -0.0784000015258789, 0.04403047150789242)
        player.assertGround()
    }
}
