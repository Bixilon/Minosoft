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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.mesh.SimpleTextureMesh
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3

class FireOverlay(
    private val renderWindow: RenderWindow,
    private val z: Float,
) : Overlay {
    private val config = renderWindow.connection.profiles.rendering.overlay.fire
    private val player = renderWindow.connection.player
    private val shader: Shader = renderWindow.shaderManager.genericTexture2dShader
    private var texture: AbstractTexture = renderWindow.textureManager.staticTextures.createTexture("block/fire_1".toResourceLocation().texture())
    override val render: Boolean
        get() {
            if (!config.enabled) {
                return false
            }
            if (player.gamemode == Gamemodes.CREATIVE && !config.creative) {
                return false
            }
            if (player.fluidHeights[DefaultFluids.LAVA] != null && !config.lava) {
                return false
            }
            return player.isOnFire
        }
    private lateinit var mesh: SimpleTextureMesh
    private val tintColor = RGBColor(1.0f, 1.0f, 1.0f, 0.9f)


    override fun postInit() {
        mesh = SimpleTextureMesh(renderWindow)

        // ToDo: Minecraft does this completely different...
        mesh.addQuad(arrayOf(
            Vec3(-2.0f, -2.4f, +0.0f),
            Vec3(-2.0f, +0.4f, +0.0f),
            Vec3(+0.0f, +0.4f, +0.0f),
            Vec3(+0.0f, -2.4f, +0.0f),
        )) { position, uv -> mesh.addVertex(position, texture, uv, tintColor) }

        mesh.addQuad(arrayOf(
            Vec3(-0.0f, -2.4f, +0.0f),
            Vec3(-0.0f, +0.4f, +0.0f),
            Vec3(+2.0f, +0.4f, +0.0f),
            Vec3(+2.0f, -2.4f, +0.0f),
        )) { position, uv -> mesh.addVertex(position, texture, uv, tintColor) }


        mesh.load()
    }


    override fun draw() {
        mesh.unload()
        postInit()
        renderWindow.renderSystem.reset(blending = true, depthTest = false)
        shader.use()
        mesh.draw()
    }


    companion object : OverlayFactory<FireOverlay> {

        override fun build(renderWindow: RenderWindow, z: Float): FireOverlay {
            return FireOverlay(renderWindow, z)
        }
    }
}
