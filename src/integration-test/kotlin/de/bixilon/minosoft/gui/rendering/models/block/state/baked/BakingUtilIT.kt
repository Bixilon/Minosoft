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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compactProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["models"])
class BakingUtilIT {

    fun `combine single properties`() {
        val raw = arrayOf(mutableListOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(raw.compactProperties(), arrayOf(SideProperties(arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)), TextureTransparencies.OPAQUE)))
    }

    fun `combine multiple identical properties`() {
        val raw = arrayOf(mutableListOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(raw.compactProperties(), arrayOf(SideProperties(arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)), TextureTransparencies.OPAQUE)))
    }

    fun `combine 2 half block properties`() {
        val raw = arrayOf(mutableListOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 0.5f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.0f, 0.5f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)))
        assertEquals(raw.compactProperties(), arrayOf(SideProperties(arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)), TextureTransparencies.OPAQUE)))
    }

    fun `combine same size but different transparency types`() {
        val raw = arrayOf(mutableListOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.TRANSPARENT)))
        assertEquals(raw.compactProperties(), arrayOf(SideProperties(arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE)), TextureTransparencies.OPAQUE)))
    }

    fun `combine same size and transparent types`() {
        val raw = arrayOf(mutableListOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.TRANSPARENT), FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.TRANSPARENT)))
        assertEquals(raw.compactProperties(), arrayOf(SideProperties(arrayOf(FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.TRANSPARENT)), TextureTransparencies.TRANSPARENT)))
    }

    fun `combine not matching size and and transparency types`() {
        val raw = arrayOf(mutableListOf(FaceProperties(Vec2(0.1f, 0.3f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.TRANSPARENT)))
        assertEquals(raw.compactProperties(), arrayOf(SideProperties(arrayOf(FaceProperties(Vec2(0.1f, 0.3f), Vec2(1.0f, 1.0f), TextureTransparencies.OPAQUE), FaceProperties(Vec2(0.0f, 0.0f), Vec2(1.0f, 1.0f), TextureTransparencies.TRANSPARENT)), TextureTransparencies.TRANSPARENT)), null)
    }
}
