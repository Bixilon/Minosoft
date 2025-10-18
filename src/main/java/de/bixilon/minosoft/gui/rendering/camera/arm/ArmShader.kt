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

package de.bixilon.minosoft.gui.rendering.camera.arm

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.shader.types.TintedShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager

class ArmShader(override val native: NativeShader) : Shader(), TintedShader, TextureShader {
    override var textures: TextureManager by textureManager()
    var texture by uniform("uIndexLayerAnimation", 0x00, NativeShader::setUInt)
    override var tint by uniform("uTintColor", ChatColors.WHITE.rgb())
    var skinParts by uniform("uSkinParts", 0xFF, NativeShader::setUInt)
    var transform by uniform("uTransform", Mat4f.EMPTY)
}
