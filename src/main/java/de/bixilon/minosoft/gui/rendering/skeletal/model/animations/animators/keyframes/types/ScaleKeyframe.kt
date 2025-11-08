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

package de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types

import com.fasterxml.jackson.annotation.JsonCreator
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.Vec3KeyframeInstance
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.KeyframeInterpolation
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.SkeletalKeyframe
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.SkeletalKeyframe.Companion.toKeyframes
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration

data class ScaleKeyframe(
    val interpolation: KeyframeInterpolation = KeyframeInterpolation.NONE,
    override val loop: AnimationLoops,
    val data: List<KeyframeData<Vec3f>>,
) : SkeletalKeyframe {
    override val type get() = TYPE

    @JsonCreator
    constructor(interpolation: KeyframeInterpolation, loop: AnimationLoops, data: Map<Any, Any>) : this(interpolation, loop, data.toKeyframes())

    init {
        if (data.size < 2) throw IllegalArgumentException("Must have at least 2 keyframes!")
    }

    override fun instance() = object : Vec3KeyframeInstance(data, loop, interpolation) {
        override fun apply(value: Vec3f, transform: TransformInstance) {
            transform.matrix
                .scaleAssign(value)
        }
    }

    companion object {
        const val TYPE = "scale"
    }
}
