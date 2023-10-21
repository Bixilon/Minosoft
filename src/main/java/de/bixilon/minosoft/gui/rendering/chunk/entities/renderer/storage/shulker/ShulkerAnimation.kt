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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.shulker

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.OpenCloseAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rad

class ShulkerAnimation(
    instance: SkeletalInstance,
) : OpenCloseAnimation(instance) {
    override val transform = instance.transform.children[TRANSFORM]!!

    override val name get() = NAME

    override val closingDuration get() = 0.5f
    override val openingDuration get() = 0.5f

    override fun transform() {
        val rotation = interpolateLinear(this.progress, ROTATION_CLOSED, ROTATION_OPENED)
        val translation = interpolateLinear(this.progress, TRANSLATION_CLOSED, TRANSLATION_OPENED)
        transform.value
            .translateAssign(translation)
            .translateAssign(transform.pivot)
            .rotateRadAssign(rotation)
            .translateAssign(-transform.pivot)
    }


    companion object {
        const val TRANSFORM = "lid"
        const val NAME = "shulker"

        private val ROTATION_CLOSED = Vec3(0.0f, 0.0f, 0.0f).rad
        private val ROTATION_OPENED = Vec3(0.0f, 270.0f, 0.0f).rad

        private val TRANSLATION_CLOSED = Vec3(0.0f, 0.0f, 0.0f)
        private val TRANSLATION_OPENED = Vec3(0.0f, 0.5f, 0.0f)
    }
}
