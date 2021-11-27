/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX
import org.lwjgl.opengl.NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX

object NvidiaOpenGLVendor : OpenGLVendor {
    override val strictSpecification: Boolean = false
    override val shaderDefine: String = "__NVIDIA"

    override val availableVRAM: Long
        get() = glGetInteger(GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX).toLong() * 1024

    override val maximumVRAM: Long
        get() = glGetInteger(GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX).toLong() * 1024

}
