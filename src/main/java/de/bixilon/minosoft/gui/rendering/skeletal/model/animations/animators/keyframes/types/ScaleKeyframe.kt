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

package de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.Vec3KeyframeInstance
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.KeyframeInterpolation
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.SkeletalKeyframe
import java.util.*

data class ScaleKeyframe(
    val interpolation: KeyframeInterpolation = KeyframeInterpolation.NONE,
    override val loop: AnimationLoops,
    val data: TreeMap<Float, Vec3>,
) : SkeletalKeyframe {
    override val type get() = TYPE

    override fun instance() = object : Vec3KeyframeInstance(data, loop, interpolation) {
        override fun apply(value: Vec3, transform: TransformInstance) {
            transform.value
                .scaleAssign(value)
        }
    }

    companion object {
        const val TYPE = "scale"
    }
}
