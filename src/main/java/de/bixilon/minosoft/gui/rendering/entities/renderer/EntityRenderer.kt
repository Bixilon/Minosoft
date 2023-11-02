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

package de.bixilon.minosoft.gui.rendering.entities.renderer

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.ColorUtil
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.easteregg.EntityEasterEggs.FLIP_ROTATION
import de.bixilon.minosoft.gui.rendering.entities.easteregg.EntityEasterEggs.isFlipped
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.FeatureManager
import de.bixilon.minosoft.gui.rendering.entities.hitbox.HitboxFeature
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.reset
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.util.interpolate.Interpolator

abstract class EntityRenderer<E : Entity>(
    val renderer: EntitiesRenderer,
    val entity: E,
) {
    private var update = 0L
    val features = FeatureManager(this)
    val info = entity.renderInfo

    val hitbox = HitboxFeature(this).register()
    val light = Interpolator(ChatColors.WHITE, ColorUtil::interpolateRGB)
    val matrix = Mat4()
    var visible = true
        protected set

    fun <T : EntityRenderFeature> T.register(): T {
        features += this
        return this
    }

    open fun updateMatrix(delta: Float) {
        val position = Vec3(entity.renderInfo.position - renderer.context.camera.offset.offset)
        matrix.reset()
        matrix.translateAssign(position)

        if (entity.isFlipped()) {
            matrix
                .translateAssign(Vec3(0.0f, entity.dimensions.y + 0.2f, 0.0f))
                .rotateRadAssign(FLIP_ROTATION)
        }
    }

    open fun update(millis: Long) {
        val delta = if (this.update <= 0L) 0.0f else ((millis - update) / 1000.0f)
        updateLight(delta)
        entity.draw(millis)
        updateMatrix(delta)
        features.update(millis, delta)
        this.update = millis
    }

    private fun getCurrentLight(): Int {
        var light = entity.physics.positionInfo.chunk?.light?.get(entity.physics.positionInfo.inChunkPosition) ?: return 0xFF
        if (entity.isOnFire) {
            light = light or 0xF0
        }
        return light
    }

    protected open fun updateLight(delta: Float) {
        if (this.light.delta >= 1.0f) {
            val rgb = renderer.context.light.map.buffer[getCurrentLight()]
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
        this.visible = visible && !entity.isInvisible
        features.updateVisibility(occluded)
    }
}
