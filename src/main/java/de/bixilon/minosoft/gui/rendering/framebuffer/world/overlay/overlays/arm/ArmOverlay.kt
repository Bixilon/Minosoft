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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.arm

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.ArmAnimator
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.PlayerModel
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.util.KUtil.minosoft

class ArmOverlay(private val renderWindow: RenderWindow) : Overlay {
    private val config = renderWindow.connection.profiles.rendering.overlay
    private val shader = renderWindow.renderSystem.createShader(minosoft("arm")) { ArmOverlayShader(it) }
    override val render: Boolean
        get() = renderWindow.camera.view.view.renderArm && config.arm.render
    private var arm = renderWindow.connection.player.mainArm // TODO: camera player entity
    private var skin: DynamicTexture? = null
    private var model: PlayerModel? = null
    private var mesh: ArmMesh = unsafeNull()
    private var animator: ArmAnimator? = null

    private var a = 0.0f

    override fun postInit() {
        shader.load()
        updateMesh()
    }

    private fun updateMesh() {
        this.mesh = ArmMesh(renderWindow)
        val skin = this.skin ?: renderWindow.textureManager.steveTexture
        this.model?.instance?.model?.model?.let { this.mesh.addArm(it, arm, skin) }
        this.mesh.load()
    }


    override fun update() {
        val arm = renderWindow.connection.player.mainArm
        if (arm != this.arm) {
            this.mesh.unload()
            this.arm = arm
            init()
        }
        val model = renderWindow.connection.player.model.nullCast<PlayerModel>()
        this.model = model
        this.animator = model?.let { ArmAnimator(it) }
        val skin = model?.skin
        if (this.skin == skin) {
            return
        }
        this.skin?.usages?.decrementAndGet()
        skin?.usages?.incrementAndGet()
        this.skin = skin
    }

    private fun calculateTransform(): Mat4 {
        val matrix = Mat4()
        matrix.translateAssign(Vec3(-0.5, -0.5, 0))
        a += 1f



        return matrix
    }

    override fun draw() {
        val skin = this.skin ?: return
        renderWindow.renderSystem.disable(RenderingCapabilities.FACE_CULLING)
        mesh.unload()
        updateMesh()
        shader.use()
        shader.transform = calculateTransform()
        shader.textureIndexLayer = skin.shaderId
        mesh.draw()
    }


    companion object : OverlayFactory<ArmOverlay> {
        override fun build(renderWindow: RenderWindow): ArmOverlay {
            return ArmOverlay(renderWindow)
        }
    }
}
