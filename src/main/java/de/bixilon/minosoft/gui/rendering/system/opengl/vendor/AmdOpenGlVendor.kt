/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.opengl.vendor

import de.bixilon.minosoft.gui.rendering.system.base.driver.DriverHacks
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.ATIMeminfo.GL_VBO_FREE_MEMORY_ATI
import org.lwjgl.opengl.GL11.glGetInteger

object AmdOpenGlVendor : OpenGlVendor {
    override val shaderDefine: String = "__AMD"

    override val availableVRAM: Long
        get() = gl { glGetInteger(GL_VBO_FREE_MEMORY_ATI) }.toLong() * 1024


    override val hacks = DriverHacks.set(
        DriverHacks.USE_QUADS_OVER_TRIANGLE,
    )

    override fun toString() = "amd"
}
