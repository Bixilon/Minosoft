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

package de.bixilon.minosoft.gui.rendering.entities.feature.hitbox

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.math.interpolation.Interpolator
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.ColorInterpolation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.entities.feature.properties.MeshedFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.integrated.LineMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil
import de.bixilon.minosoft.protocol.network.session.play.tick.TickUtil
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

class HitboxFeature(renderer: EntityRenderer<*>) : MeshedFeature<Mesh>(renderer) {
    private val manager = renderer.renderer.features.hitbox

    private var aabb = AABB.EMPTY
    private var eyePosition = Vec3f.EMPTY
    private var rotation = EntityRotation.EMPTY

    private var color = Interpolator(renderer.entity.hitboxColor ?: ChatColors.WHITE, ColorInterpolation::interpolateRGBA)
    private var velocity = Interpolator(Vec3f.EMPTY, Vec3fUtil::interpolateLinear)

    override fun update(time: ValueTimeMark, delta: Float) {
        if (!manager.enabled) return unload()
        if (!_enabled) return unload()
        if (renderer.entity.isInvisible(renderer.renderer.session.camera.entity) && !manager.profile.showInvisible) return unload()
        if (!visible) return

        val offset = renderer.renderer.context.camera.offset.offset

        val update = updateRenderInfo(offset) or interpolate(delta)

        if (this.mesh != null && !update) return

        unload()
        createMesh()
    }


    private fun updateRenderInfo(offset: BlockPosition): Boolean {
        var changes = 0

        val renderInfo = renderer.entity.renderInfo
        val aabb = renderInfo.cameraAABB
        val eyePosition = Vec3f(renderInfo.eyePosition - offset)
        val rotation = renderInfo.rotation

        if (aabb != this.aabb) {
            this.aabb = aabb; changes++
        }
        if (eyePosition != this.eyePosition) {
            this.eyePosition = eyePosition; changes++
        }
        if (rotation != this.rotation) {
            this.rotation = rotation; changes++
        }

        return changes > 0
    }

    private fun interpolate(delta: Float): Boolean {
        if (color.delta >= 1.0f) {
            this.color.push(renderer.entity.hitboxColor ?: ChatColors.WHITE)
        }
        this.color.add(delta, 0.3f)

        if (velocity.delta >= 1.0f) {
            this.velocity.push(Vec3f(renderer.entity.physics.velocity))
        }
        this.velocity.add(delta, (TickUtil.TIME_PER_TICK / 1.seconds).toFloat())


        return !this.color.identical || !this.velocity.identical
    }

    private fun createMesh() {
        val mesh = LineMeshBuilder(renderer.renderer.context)

        val color = color.value
        if (manager.profile.lazy) {
            mesh.drawLazyAABB(aabb, color)
        } else {
            mesh.drawAABB(aabb, color = color)
        }

        val center = Vec3f(aabb.center)
        val velocity = velocity.value
        if (velocity.length2() > 0.003f) {
            mesh.drawLine(center, center + velocity * 5.0f, color = ChatColors.YELLOW)
        }

        mesh.drawLine(eyePosition, eyePosition + rotation.front * 5.0f, color = ChatColors.BLUE)

        this.mesh = mesh.bake()
    }


    override fun draw(mesh: Mesh) {
        // TODO: update position with shader uniform
        val system = renderer.renderer.context.system
        if (manager.profile.showThroughWalls) {
            system.reset(depth = DepthFunctions.ALWAYS)
        } else {
            system.reset()
        }
        manager.shader.use()
        super.draw(mesh)
    }
}
