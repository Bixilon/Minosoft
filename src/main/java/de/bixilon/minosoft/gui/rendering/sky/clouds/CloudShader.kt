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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.camera.FogManager
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.types.FogShader
import de.bixilon.minosoft.gui.rendering.shader.types.ViewProjectionShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

class CloudShader(
    override val native: NativeShader,
) : Shader(), ViewProjectionShader, FogShader {
    override var viewProjectionMatrix: Mat4 by viewProjectionMatrix()
    override var cameraPosition: Vec3 by cameraPosition()
    override var fog: FogManager by fog()

    var cloudsColor by uniform("uCloudsColor", Vec3.EMPTY)
    var offset by uniform("uOffset", 0.0f)
    var yOffset by uniform("uYOffset", 0.0f)
}
