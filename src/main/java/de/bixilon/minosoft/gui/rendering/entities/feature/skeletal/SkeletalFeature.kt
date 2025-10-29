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

package de.bixilon.minosoft.gui.rendering.entities.feature.skeletal

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.minosoft.gui.rendering.entities.easteregg.EntityEasterEggs.isFlipped
import de.bixilon.minosoft.gui.rendering.entities.feature.DrawableEntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.LivingEntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

open class SkeletalFeature(
    renderer: EntityRenderer<*>,
    val instance: SkeletalInstance,
) : DrawableEntityRenderFeature(renderer) {
    protected val manager = renderer.renderer.context.skeletal
    private val rotation = MVec3f()

    protected var position = Vec3d.EMPTY
    protected var yaw = 0.0f

    constructor(renderer: EntityRenderer<*>, model: BakedSkeletalModel) : this(renderer, model.createInstance(renderer.renderer.context))


    protected open fun updatePosition() {
        val renderInfo = renderer.info
        val yaw = renderInfo.rotation.yaw
        val position = renderInfo.position

        var changes = 0
        if (this.position != position) {
            changes++
            this.position = position
        }
        if (this.yaw != yaw) {
            changes++
            this.yaw = yaw
        }
        if (changes == 0) return
        if (renderer.entity.isFlipped()) {
            this.yaw *= -1.0f
        }
        updateInstance()
    }

    protected open fun updateInstance() {
        this.rotation.y = -yaw.rad
        instance.update(this.rotation.unsafe, renderer.matrix.unsafe)
    }

    override fun update(time: ValueTimeMark, delta: Duration) {
        super.update(time, delta)
        instance.transform.reset()
        updatePosition()
        instance.animation.draw(delta)
    }

    override fun prepare() {
        super.prepare()
        instance.transform.transform(instance.matrix.unsafe)
    }

    override fun draw() {
        var tint = renderer.light.value
        if (renderer is LivingEntityRenderer<*>) {
            tint *= renderer.damage.value
        }
        instance.draw(tint)
    }


    override fun unload() {
        super.unload()
        instance.unload()
    }

    override fun invalidate() {
        super.invalidate()
        this.position = Vec3d.EMPTY
        this.yaw = 0.0f
    }
}
