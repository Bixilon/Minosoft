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

package de.bixilon.minosoft.gui.rendering.entities.feature

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY

open class SkeletalFeature(
    renderer: EntityRenderer<*>,
    val instance: SkeletalInstance,
) : EntityRenderFeature(renderer) {
    protected val manager = renderer.renderer.context.skeletal

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

        val rotation = Vec3(0.0f, (EntityRotation.HALF_CIRCLE_DEGREE - yaw).rad, 0.0f)
        instance.update(rotation, renderer.matrix)
    }

    override fun update(millis: Long, delta: Float) {
        instance.transform.reset()
        updatePosition()
        instance.animation.draw(delta)
    }

    override fun draw() {
        instance.draw(renderer.light.value)
    }

    override fun reset() {
        this.position = Vec3d.EMPTY
        this.yaw = 0.0f
    }
}
