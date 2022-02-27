/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ShaderManager(
    val renderWindow: RenderWindow,
) {
    val genericColorShader = renderWindow.renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "generic/color"))
    val genericTextureShader = renderWindow.renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "generic/texture"))
    val genericTexture2dShader = renderWindow.renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "generic/texture_2d"))
    val entitiesShader = renderWindow.renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "entities"))

    private fun Shader.loadAnimated() {
        load()
        renderWindow.textureManager.staticTextures.use(this)
        renderWindow.textureManager.staticTextures.animator.use(this)
    }

    fun postInit() {
        genericColorShader.load()
        genericTextureShader.loadAnimated()
        genericTexture2dShader.loadAnimated()
        entitiesShader.loadAnimated()
        renderWindow.lightMap.use(entitiesShader)
    }
}
