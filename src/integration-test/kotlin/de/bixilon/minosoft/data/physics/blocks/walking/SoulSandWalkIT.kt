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
class SoulSandWalkIT : WalkIT() {

    @Test(priority = -1)
    fun getSoulSand() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.SOUL_SAND]?.defaultState ?: throw SkipException("Can not find soul sand!")
    }

    fun soulSandLanding() {
        val player = super.landing()

        player.assertPosition(6.0, 0.875, 6.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun soulSandWalking1() {
        val player = super.walking1()

        player.assertPosition(6.0, 0.9215999984741211, 6.046334400178063)
        player.assertVelocity(0.0, -0.1552320045166016, 0.009731322110889201)
    }

    fun soulSandWalking2() {
        val player = super.walking2()

        player.assertPosition(6.0, 0.875, 6.931977406188496)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383437597522438)
    }

    fun soulSandWalking3() {
        val player = super.walking3()

        player.assertPosition(6.0, 0.875, 11.94733037737146)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383833577600523)
    }

    fun soulSandWalking4() {
        val player = super.walking4()

        player.assertPosition(-6.0, 0.875, -5.068022593811504)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383437597522438)
    }

    fun soulSandWalking5() {
        val player = super.walking5()

        player.assertPosition(-6.0, 0.875, -0.05266962262853264)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383833577600523)
    }

    fun soulSandWalking6() {
        val player = super.walking6()

        player.assertPosition(-6.0, 0.875, -6.931977406188496)
        player.assertVelocity(-2.6211365851074295E-18, -0.0784000015258789, -0.027383437597522438)
    }

    fun soulSandWalking7() {
        val player = super.walking7()

        player.assertPosition(-6.0, 0.875, -11.94733037737146)
        player.assertVelocity(-2.6211365851074295E-18, -0.0784000015258789, -0.027383833577600523)
    }

    fun soulSandWalking8() {
        val player = super.walking8()

        player.assertPosition(-6.033364455376936, 0.875, -5.713174166169708)
        player.assertVelocity(0.027383826816441414, -0.0784000015258789, 0.0)
    }

    fun soulSandWalking9() {
        val player = super.walking9()

        player.assertPosition(-14.990138376462085, 0.875, -1.4447659997606854)
        player.assertVelocity(-0.02279835578593394, -0.0784000015258789, -0.015165410888227721)
    }

    fun soulSandSprintJump1() {
        val player = super.sprintJump1()

        player.assertPosition(-6.0, 0.9215999984741211, -5.947785598743689)
        player.assertVelocity(0.0, -0.1552320045166016, 0.011871642596948414)
    }

    fun soulSandSprintJump2() {
        val player = super.sprintJump2()

        player.assertPosition(-6.0, 1.6281999805212017, -5.4694844005672305)
        player.assertVelocity(0.0, 0.24813599859094576, 0.09095772429108878)
    }

    fun soulSandSlabWalk() {
        val player = super.slabWalk()

        player.assertPosition(-6.0, 1.5, -5.566723500360934)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }
}
