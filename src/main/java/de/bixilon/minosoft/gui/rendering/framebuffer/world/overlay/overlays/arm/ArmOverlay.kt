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

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.PlayerModel
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.Overlay
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayFactory
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel.Companion.fromBlockCoordinates
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.RenderingCapabilities
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.PlayerSkin
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.KUtil.minosoft

class ArmOverlay(private val context: RenderContext) : Overlay {
    private val config = context.connection.profiles.rendering.overlay
    private val shader = context.renderSystem.createShader(minosoft("arm")) { ArmOverlayShader(it) }
    override val render: Boolean
        get() = context.camera.view.view.renderArm && config.arm.render
    private var arm = context.connection.player.mainArm // TODO: camera player entity
    private var skin: PlayerSkin? = null
    private var model: PlayerModel? = null
    private var mesh: ArmMesh = unsafeNull()

    private var refresh = false

    private var refreshTransform = true

    override fun init() {
        context.connection.profiles.connection::mainArm.observe(this, true) { this.arm = it; refresh = true;refreshTransform = true }
        context.connection.events.listen<ResizeWindowEvent> { this.refreshTransform = true }
    }

    private fun poll() {
        val model = context.connection.player.model.nullCast<PlayerModel>()
        val skin = model?.skin
        // TODO: check skin parts
        if (this.model == model && this.skin == skin) {
            return
        }
        this.model = model
        skin?.texture?.usages?.incrementAndGet()
        this.skin?.texture?.usages?.decrementAndGet()
        this.skin = skin

        refresh = true
    }

    override fun postInit() {
        shader.load()
        createMesh()
    }

    private fun createMesh() {
        this.mesh = ArmMesh(context)
        val skin = this.skin
        val model = this.model?.instance?.model?.model
        if (model != null && skin != null) {
            this.mesh.addArm(model, arm, skin.texture)
        }
        this.mesh.load()
    }


    override fun update() {
        poll()
        if (!refresh) {
            return
        }
        this.mesh.unload()
        createMesh()
        this.refresh = false
    }

    private fun calculateTransform(): Mat4 {
        val screen = context.window.sizef
        val aspect = screen.x / screen.y
        val projection = GLM.perspective(60.0f.rad, aspect, CameraDefinition.NEAR_PLANE, CameraDefinition.FAR_PLANE)

        val model = this.model ?: return Mat4()
        val outliner = model.instance?.model?.model?.outliner?.find { it.name == if (arm == Arms.LEFT) "LEFT_ARM" else "RIGHT_ARM" } ?: return Mat4()

        val matrix = FirstPersonArmAnimator(model).calculateTransform(outliner, 0.0f)
        val screenMatrix = Mat4()

        val translation = Vec3(if (arm == Arms.LEFT) -0.08f else 0.08f, 0, 0)

        if (aspect > 1.8f) {
            translation.x *= aspect * 1.8f
        }
        screenMatrix.translateAssign(translation) // move inner side of arm to 0|0|0

        screenMatrix.translateAssign(Vec3(if (arm == Arms.LEFT) -18 else -12, -54, -10).fromBlockCoordinates())

        this.refreshTransform = false
        return projection * screenMatrix * matrix
    }

    override fun draw() {
        val skin = this.skin ?: return
        context.renderSystem.clear(IntegratedBufferTypes.DEPTH_BUFFER)

        context.renderSystem.disable(RenderingCapabilities.FACE_CULLING)
        context.renderSystem.enable(RenderingCapabilities.DEPTH_TEST)
        context.renderSystem.enable(RenderingCapabilities.BLENDING)
        context.renderSystem.depthMask = true

        shader.use()
        shader.textureIndexLayer = skin.texture.shaderId

        if (refreshTransform) {
            shader.transform = calculateTransform()
        }

        mesh.draw()
        context.renderSystem.clear(IntegratedBufferTypes.DEPTH_BUFFER)
    }


    companion object : OverlayFactory<ArmOverlay> {
        override fun build(context: RenderContext): ArmOverlay {
            return ArmOverlay(context)
        }
    }
}
