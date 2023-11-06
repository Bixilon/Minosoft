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

package de.bixilon.minosoft.gui.rendering.font.types.dummy

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.font.renderer.code.AscentedCodePointRenderer
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture

class DummyCodePointRenderer(
    override val uvStart: Vec2 = Vec2(0.1f, 0.2f),
    override val uvEnd: Vec2 = Vec2(0.6f, 0.7f),
    override val width: Float = 5.0f,
    override val ascent: Float = 7.0f,
    override val height: Float = 8.0f,
) : AscentedCodePointRenderer {
    override val texture = DummyTexture()
}
