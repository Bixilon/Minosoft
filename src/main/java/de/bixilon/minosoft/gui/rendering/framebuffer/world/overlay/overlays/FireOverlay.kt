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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.integrated.SimpleTextureMeshBuilder

class FireOverlay(
    private val context: RenderContext,
) : Overlay {
    private val config = context.session.profiles.rendering.overlay.fire
    private val player = context.session.player
    private val shader = context.shaders.genericTexture2dShader
    private var texture: Texture = context.textures.static.create(if (context.session.version.flattened) TEXTURE else LEGACY_TEXTURE)
    private val lava = context.session.registries.fluid[LavaFluid]
    override val render: Boolean
        get() {
            if (!config.enabled) {
                return false
            }
            if (player.gamemode == Gamemodes.CREATIVE && !config.creative) {
                return false
            }
            if (player.physics.submersion[lava] > 0.0 && !config.lava) {
                return false
            }
            return player.isOnFire
        }
    private lateinit var mesh: Mesh
    private val tintColor = RGBAColor(1.0f, 1.0f, 1.0f, 0.9f)


    private fun updateMesh() {
        val mesh = SimpleTextureMeshBuilder(context)

        // ToDo: Minecraft does this completely different...#
        // TODO: flip upside down
        mesh.addQuad(arrayOf(
            Vec3f(-2.0f, -2.4f, +0.0f),
            Vec3f(-2.0f, +0.4f, +0.0f),
            Vec3f(+0.0f, +0.4f, +0.0f),
            Vec3f(+0.0f, -2.4f, +0.0f),
        )) { position, uv -> mesh.addVertex(position, texture, uv, tintColor) }

        mesh.addQuad(arrayOf(
            Vec3f(-0.0f, -2.4f, +0.0f),
            Vec3f(-0.0f, +0.4f, +0.0f),
            Vec3f(+2.0f, +0.4f, +0.0f),
            Vec3f(+2.0f, -2.4f, +0.0f),
        )) { position, uv -> mesh.addVertex(position, texture, uv, tintColor) }

        this.mesh = mesh.bake()
        this.mesh.load()
    }

    override fun postInit() {
        updateMesh()
    }


    override fun draw() {
        mesh.unload()
        updateMesh()
        context.system.reset(blending = true, depthTest = false)
        shader.use()
        mesh.draw()
    }


    companion object : OverlayFactory<FireOverlay> {
        private val TEXTURE = minecraft("block/fire_1").texture()
        private val LEGACY_TEXTURE = minecraft("blocks/fire_layer_1").texture()

        override fun build(context: RenderContext): FireOverlay {
            return FireOverlay(context)
        }
    }
}
