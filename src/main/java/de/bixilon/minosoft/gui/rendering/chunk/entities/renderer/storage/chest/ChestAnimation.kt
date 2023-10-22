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
import de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.OpenCloseAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateSine
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rad

class ChestAnimation(
    instance: SkeletalInstance,
) : OpenCloseAnimation(instance) {
    override val transform = instance.transform.children[TRANSFORM]!!

    override val name get() = NAME

    override val closingDuration get() = 0.3f
    override val openingDuration get() = 0.4f


    override fun transform() {
        val rotation = interpolateSine(this.progress, BASE, OPEN)
        transform.value
            .translateAssign(transform.pivot)
            .rotateRadAssign(rotation)
            .translateAssign(transform.nPivot)
    }


    companion object {
        const val TRANSFORM = "lid"
        const val NAME = "chest"

        private val BASE = Vec3(0.0f, 0.0f, 0.0f).rad
        private val OPEN = Vec3(90.0f, 0.0f, 0.0f).rad
    }
}
