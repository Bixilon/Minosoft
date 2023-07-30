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

import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.applySpeed
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SpeedGrassWalkIT : WalkIT() {

    @Test(priority = -1)
    fun getGrassBlock() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.GRASS_BLOCK]?.states?.default ?: throw SkipException("Can not find grass block!")
    }

    override fun createPlayer(connection: PlayConnection): LocalPlayerEntity {
        val player = super.createPlayer(connection)
        player.applySpeed(2)
        return player
    }

    fun grassLanding() {
        val player = super.landing()

        player.assertPosition(6.0, 1.0, 6.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun grassWalking1() {
        val player = super.walking1()

        player.assertPosition(6.0, 1.0, 6.057036000349253)
        player.assertVelocity(0.0, -0.0784000015258789, 0.034066761351146994)
    }

    fun grassWalking2() {
        val player = super.walking2()

        player.assertPosition(6.0, 1.0, 8.482394558069705)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18735412633674173)
    }

    fun grassWalking3() {
        val player = super.walking3()

        player.assertPosition(6.0, 0.7663679939575195, 22.15748638036824)
        player.assertVelocity(0.0, -0.230527368912964, 0.18943879771425812)
    }

    fun grassWalking4() {
        val player = super.walking4()

        player.assertPosition(-6.0, 1.0, -3.517605441930293)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18735412633674173)
    }

    fun grassWalking5() {
        val player = super.walking5()

        player.assertPosition(-6.0, 1.0, 10.294686379971871)
        player.assertVelocity(0.0, -0.0784000015258789, 0.18857449703980356)
    }

    fun grassWalking6() {
        val player = super.walking6()

        player.assertPosition(-6.0, 1.0, -8.482394558069705)
        player.assertVelocity(-1.0484545793618027E-17, -0.0784000015258789, -0.18735412633674173)
    }

    fun grassWalking7() {
        val player = super.walking7()

        player.assertPosition(-6.0, -0.14510670065164383, -21.750768038282843)
        player.assertVelocity(-2.1842801829110955E-18, -0.44749789698341763, -0.19159235076014583)
    }

    fun grassWalking8() {
        val player = super.walking8()

        player.assertPosition(-6.408389862541348, 1.0, -5.441348673920244)
        player.assertVelocity(0.18813046951796888, -0.0784000015258789, 0.0)
    }

    fun grassWalking9() {
        val player = super.walking9()

        player.assertPosition(-26.94764215417615, -31.121101718604383, 8.764840346975888)
        player.assertVelocity(-0.17946317107449355, -1.8663786271174325, -0.07330073509208593)
    }

    fun grassSprintJump1() {
        val player = super.sprintJump1()

        player.assertPosition(-6.0, 1.0, -5.937083998572499)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03941756248656188)
    }

    fun grassSprintJump2() {
        val player = super.sprintJump2()

        player.assertPosition(-6.0, 2.0013359791121474, -4.957423968006168)
        player.assertVelocity(0.0, 0.16477328182606651, 0.2447024583059974)
    }

    fun soulSandSlabWalk() {
        val player = super.slabWalk()

        player.assertPosition(-6.0, 1.5, -4.8492709019026305)
        player.assertVelocity(0.0, -0.0784000015258789, 0.007930162975812486)
        player.assertGround()
    }
}
