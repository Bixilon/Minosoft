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

package de.bixilon.minosoft.gui.rendering.models.fluid.fluids

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.fluid.FluidModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class LavaFluidModel : FluidModel {
    override var still: Texture? = null
    override var flowing: Texture? = null
    override var properties = FaceProperties(Vec2.EMPTY, Vec2(1.0f), TextureTransparencies.OPAQUE) // TODO: determinate by texture

    override fun load(context: RenderContext) {
        still = context.textures.staticTextures.createTexture(STILL)
        flowing = context.textures.staticTextures.createTexture(FLOWING)
    }

    companion object {
        val STILL = minecraft("block/lava_still").texture()
        val FLOWING = minecraft("block/lava_flow").texture()
    }
}
