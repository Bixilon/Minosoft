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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.chest

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.AbstractAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.NOT_OVER
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.OVER
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateSine

class ChestAnimation(
    private val instance: SkeletalInstance,
) : AbstractAnimation {
    private val transform = instance.transform.children[TRANSFORM]!!
    private var progress = 0.0f
    private var opening = true

    override val name get() = NAME

    override fun draw(delta: Float): Boolean {
        return if (opening) drawOpening(delta) else drawClosing(delta)
    }

    private fun drawOpening(delta: Float): Boolean {
        this.progress += delta / OPENING_TIME
        if (this.progress > 1.0f) {
            this.progress = 1.0f
        }
        transform()
        return NOT_OVER
    }

    private fun drawClosing(delta: Float): Boolean {
        this.progress -= delta / CLOSING_TIME
        if (progress <= 0.0f) {
            this.progress = 0.0f
            return OVER
        }
        transform()
        return NOT_OVER
    }

    private fun transform() {
        val rotation = interpolateSine(this.progress, BASE, OPEN)
        transform.value
            .translateAssign(-transform.pivot)
            .rotateRadAssign(rotation)
            .translateAssign(transform.pivot)
    }

    fun open() {
        opening = true
    }

    fun close() {
        opening = false
    }


    companion object {
        const val TRANSFORM = "lid"
        const val NAME = "chest"

        const val OPENING_TIME = 0.5f
        const val CLOSING_TIME = 0.3f

        private val BASE = Vec3(0.0f, 0.0f, 0.0f)
        private val OPEN = Vec3(-90.0f, 0.0f, 0.0f)
    }
}
