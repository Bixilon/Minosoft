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

package de.bixilon.minosoft.gui.rendering.modding.events

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Rendering
import glm_.mat4x4.Mat4d

class CameraMatrixChangeEvent(
    renderWindow: RenderWindow = Rendering.currentContext!!,
    viewMatrix: Mat4d,
    projectionMatrix: Mat4d,
    viewProjectionMatrix: Mat4d,
) : RenderEvent(renderWindow) {
    val viewMatrix: Mat4d = viewMatrix
        get() = Mat4d(field)

    val projectionMatrix: Mat4d = projectionMatrix
        get() = Mat4d(field)

    val viewProjectionMatrix: Mat4d = viewProjectionMatrix
        get() = Mat4d(field)
}
