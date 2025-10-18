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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.camera.fog.FogManager
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.types.FogShader
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.shader.types.TintedShader
import de.bixilon.minosoft.gui.rendering.shader.types.ViewProjectionShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

class BillboardTextShader(
    override val native: NativeShader,
) : Shader(), TextureShader, ViewProjectionShader, FogShader, TintedShader {
    override var textures: TextureManager by textureManager()
    override var viewProjectionMatrix: Mat4f by viewProjectionMatrix()
    override var cameraPosition: Vec3f by cameraPosition()
    override var fog: FogManager by fog()
    var matrix: Mat4f by uniform("uMatrix", Mat4f())

    override var tint by uniform("uTintColor", ChatColors.WHITE.rgb())
}
