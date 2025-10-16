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

package de.bixilon.minosoft.gui.rendering.system.window

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.gui.rendering.RenderingOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import de.bixilon.minosoft.util.system.SystemUtil

interface Window {
    val systemScale: Vec2f

    var size: Vec2i
    var minSize: Vec2i
    var maxSize: Vec2i

    var visible: Boolean
    var resizable: Boolean
    var fullscreen: Boolean

    var swapInterval: Int

    var cursorMode: CursorModes
    var cursorShape: CursorShapes


    var clipboardText: String
    var title: String
    val version: String

    val time: Double

    val iconified: Boolean
    val focused: Boolean

    fun init(profile: RenderingProfile) {
        resizable = true
        profile.advanced::swapInterval.observeRendering(this, true) { swapInterval = it }

        if (!RenderingOptions.cursorCatch) {
            cursorMode = CursorModes.DISABLED
        }
        size = DEFAULT_WINDOW_SIZE
        minSize = DEFAULT_MINIMUM_WINDOW_SIZE
        maxSize = DEFAULT_MAXIMUM_WINDOW_SIZE
    }

    fun destroy()

    fun close()

    fun forceClose()

    fun swapBuffers()

    fun pollEvents()

    fun setOpenGLVersion(major: Int, minor: Int, coreProfile: Boolean)


    fun setIcon(buffer: TextureBuffer)


    fun setDefaultIcon(assetsManager: AssetsManager) {
        val buffer = assetsManager[SystemUtil.ICON].readTexture()
        setIcon(buffer)
    }

    fun resetCursor() {
        cursorShape = CursorShapes.ARROW
    }

    companion object {
        val DEFAULT_WINDOW_SIZE: Vec2i
            get() = Vec2i(900, 500)
        val DEFAULT_MINIMUM_WINDOW_SIZE: Vec2i
            get() = Vec2i(300, 100)
        val DEFAULT_MAXIMUM_WINDOW_SIZE: Vec2i
            get() = Vec2i(-1, -1)
    }
}
