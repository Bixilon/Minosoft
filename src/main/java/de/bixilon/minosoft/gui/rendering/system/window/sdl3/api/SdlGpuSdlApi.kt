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

package de.bixilon.minosoft.gui.rendering.system.window.sdl3.api

import org.lwjgl.sdl.SDLGPU.*
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class SdlGpuSdlApi(
    val window: Long,
) : SdlWindowRenderApi {
    private var device = -1L
    override var swapInterval = 1

    override fun init() {
        val device = SDL_CreateGPUDevice(SDL_GPU_SHADERFORMAT_SPIRV, true, null as ByteBuffer?) // TODO: disable debug mode
        assert(device != MemoryUtil.NULL)
        this.device = device

        assert(SDL_ClaimWindowForGPUDevice(device, window))
    }

    override fun destroy() {
        SDL_ReleaseWindowFromGPUDevice(device, window)
        SDL_DestroyGPUDevice(device)
    }

    override fun begin() {
        // TODO: SDL_BeginGPURenderPass
    }

    override fun end() {
        // TODO: SDL_EndGPURenderPass
    }
}
