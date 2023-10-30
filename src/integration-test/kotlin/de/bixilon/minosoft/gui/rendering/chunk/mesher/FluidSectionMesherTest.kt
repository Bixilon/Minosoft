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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import org.testng.annotations.Test

@Test(groups = ["mesher"], dependsOnGroups = ["rendering", "block"], enabled = false)
class FluidSectionMesherTest {
    private var water: WaterFluid = unsafeNull()
    private var lava: LavaFluid = unsafeNull()

    @Test(priority = -1)
    fun load() {

    }


    private fun mesh(data: Map<Vec3i, Any>): ChunkMeshes {
        TODO()
    }

    fun `simple water without surrounding blocks`() {
        val mesh = mesh(mapOf(Vec3i(2, 2, 2) to water))
        TODO()
    }
}
