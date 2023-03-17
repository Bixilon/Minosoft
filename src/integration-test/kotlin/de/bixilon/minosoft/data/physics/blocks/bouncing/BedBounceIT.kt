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

package de.bixilon.minosoft.data.physics.blocks.bouncing

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class BedBounceIT : BounceIT() {

    @Test(priority = -1)
    fun getBed() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.RED_BED]?.defaultState ?: throw SkipException("Can not find bed!")
    }

    fun bedLanding() {
        val player = super.landing()
        player.assertPosition(5.0, 10.949414889345, 5.0)
        player.assertVelocity(0.0, -0.0318966396070199, 0.0)
        player.assertGround(false)
    }

    fun bedLongFall() {
        val player = super.longFall()
        player.assertPosition(5.0, 17.969425808565394, 5.0)
        player.assertVelocity(0.0, 0.36146168642585, 0.0)
    }

    fun bedStillJump() {
        val player = super.stillJump()
        player.assertPosition(5.0, 11.586924088213681, 5.0)
        player.assertVelocity(0.0, -0.22768848754498797, 0.0)
    }

    fun bedFallJump() {
        val player = super.fallJump()
        player.assertPosition(5.0, 10.683796840539191, 5.0)
        player.assertVelocity(0.0, -0.4448259643949201, 0.0)
    }

    fun bedSlabJump() {
        val player = super.slabJump()

        player.assertPosition(5.0, 11.995200877005914, 5.0)
        player.assertVelocity(0.0, -0.3739040364667221, 0.0)
    }
}
