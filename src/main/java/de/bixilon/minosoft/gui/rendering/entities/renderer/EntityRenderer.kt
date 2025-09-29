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

package de.bixilon.minosoft.gui.rendering.entities.renderer

import de.bixilon.kutil.math.interpolation.Interpolator
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.ColorInterpolation
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.easteregg.EntityEasterEggs.FLIP_ROTATION
import de.bixilon.minosoft.gui.rendering.entities.easteregg.EntityEasterEggs.isFlipped
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.FeatureManager
import de.bixilon.minosoft.gui.rendering.entities.feature.hitbox.HitboxFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.text.name.EntityNameFeature
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.reset
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.translateYAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

abstract class EntityRenderer<E : Entity>(
    val renderer: EntitiesRenderer,
    val entity: E,
) {
    private var update = TimeUtil.NULL
    val features = FeatureManager(this)
    val info = entity.renderInfo
    var distance: Double = 0.0
    var isInvisible: Boolean = false

    val hitbox = HitboxFeature(this).register()
    val name = EntityNameFeature(this).register()
    val light = Interpolator(ChatColors.WHITE.rgb(), ColorInterpolation::interpolateRGB)
    val matrix = Mat4()
    var visible = true
        protected set

    fun <T : EntityRenderFeature> T.register(): T {
        features += this
        return this
    }

    open fun updateMatrix(delta: Float) {
        // TODO: update on demand
        val offset = renderer.context.camera.offset.offset
        val original = entity.renderInfo.position
        val position = Vec3((original.x - offset.x).toFloat(), (original.y - offset.y).toFloat(), (original.z - offset.z).toFloat())
        matrix.reset()
        matrix.translateAssign(position)

        if (entity.isFlipped()) {
            matrix
                .translateYAssign(entity.dimensions.y + 0.2f)
                .rotateRadAssign(FLIP_ROTATION)
        }
    }

    open fun update(time: ValueTimeMark) {
        val delta = if (this.update == TimeUtil.NULL) 0.0f else ((time - update) / 1.seconds).toFloat()
        update(time, delta)
        this.update = time
    }

    open fun update(time: ValueTimeMark, delta: Float) {
        this.isInvisible = entity.isInvisible(renderer.session.camera.entity)
        updateLight(delta)
        updateRenderInfo(time)
        updateMatrix(delta)
        features.update(time, delta)
    }

    open fun prepare() {
        features.prepare()
    }

    open fun updateRenderInfo(millis: ValueTimeMark) {
        entity.draw(millis)
        this.distance = Vec3dUtil.distance2(entity.renderInfo.eyePosition, renderer.session.camera.entity.renderInfo.eyePosition)
    }

    private fun getCurrentLight(): LightLevel {
        val positionInfo = entity.physics.positionInfo
        if (positionInfo.chunk == null) return LightLevel.MAX
        var light = positionInfo.chunk.light[positionInfo.position.inChunkPosition]
        if (entity.isOnFire) {
            light = light.with(block = LightLevel.MAX_LEVEL)
        }
        return light
    }

    protected open fun updateLight(delta: Float) {
        if (this.light.delta >= 1.0f) {
            val rgb = renderer.context.light.map.buffer[getCurrentLight().index]
            this.light.push(rgb)
        }
        this.light.add(delta, 0.1f)
    }

    open fun unload() {
        features.unload()
    }

    open fun reset() {
        features.reset()
    }

    open fun updateVisibility(occluded: Boolean, visible: Boolean) {
        this.visible = visible
        features.updateVisibility(occluded)
    }
}
