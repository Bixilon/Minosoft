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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateYAssign
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

open class BillboardTextFeature(
    renderer: EntityRenderer<*>,
    text: ChatComponent?,
) : EntityRenderFeature(renderer) {
    private var mesh: BillboardTextMesh? = null
    private var info: TextRenderInfo? = null
    private var matrix = Mat4()
    var text: ChatComponent? = text
        set(value) {
            if (field == value) return
            field = value
            unload()
        }

    override fun update(millis: Long, delta: Float) {
        if (!enabled) return unload()
        if (this.mesh == null) {
            val text = this.text ?: return unload()
            if (text.length == 0) return unload()
            createMesh(text)
        }
        updateMatrix()
    }

    private fun createMesh(text: ChatComponent) {
        val mesh = BillboardTextMesh(renderer.renderer.context)
        val info = ChatComponentRenderer.render3d(renderer.renderer.context, PROPERTIES, MAX_SIZE, mesh, text)


        this.mesh = mesh
        this.info = info
    }

    private fun updateMatrix() {
        val rotation = Vec3(180.0f.rad, (EntityRotation.HALF_CIRCLE_DEGREE - renderer.entity.renderInfo.rotation.yaw).rad, 180.0f.rad)
        val matrix = Mat4()
            .translateYAssign(renderer.entity.eyeHeight + EYE_OFFSET)
            .rotateRadAssign(rotation)

        // TODO: rotate with camera (billboard)

        this.matrix = renderer.matrix * matrix
    }

    override fun draw() {
        val mesh = this.mesh ?: return
        if (mesh.state != Mesh.MeshStates.LOADED) mesh.load()
        renderer.renderer.context.system.reset(depth = DepthFunctions.ALWAYS)
        val shader = renderer.renderer.features.text.shader
        shader.use()
        shader.matrix = matrix
        mesh.draw()
    }

    override fun updateVisibility(occluded: Boolean) {
        this.visible = true
    }

    override fun unload() {
        val mesh = this.mesh ?: return
        this.mesh = null
        this.info = null
        renderer.renderer.queue += { mesh.unload() }
    }

    private companion object {
        val PROPERTIES = TextRenderProperties(allowNewLine = false)
        val MAX_SIZE = Vec2(150.0f, PROPERTIES.lineHeight)
        const val EYE_OFFSET = 0.5f
    }
}
