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

package de.bixilon.minosoft.gui.rendering.system.window.dummy

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.gui.rendering.system.window.BaseWindow
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import java.nio.ByteBuffer

class DummyWindow : BaseWindow {
    override val systemScale: Vec2 = Vec2(1.0f)
    override var size: Vec2i = Vec2i.EMPTY
    override var minSize: Vec2i = Vec2i.EMPTY
    override var maxSize: Vec2i = Vec2i.EMPTY
    override var visible: Boolean = false
    override var resizable: Boolean = true
    override var fullscreen: Boolean = false
    override var swapInterval: Int = 1
    override var cursorMode: CursorModes = CursorModes.NORMAL
    override var cursorShape: CursorShapes = CursorShapes.ARROW
    override var clipboardText: String = ""
    override var title: String = ""
    override val version: String = "dummy"
    override val time: Double get() = TimeUtil.millis() / 1000.0
    override val iconified: Boolean by observed(false)
    override val focused: Boolean by observed(false)

    override fun destroy() = Unit

    override fun close() = Unit

    override fun forceClose() = Unit

    override fun swapBuffers() {
        Thread.sleep(20)
    }

    override fun pollEvents() = Unit

    override fun setOpenGLVersion(major: Int, minor: Int, coreProfile: Boolean) = Unit

    override fun setIcon(size: Vec2i, buffer: ByteBuffer) = Unit
}
