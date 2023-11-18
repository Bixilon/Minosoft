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

package de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators

import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.KeyframeAnimator
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.SkeletalKeyframe

data class SkeletalAnimator(
    val transform: String,
    val keyframes: List<SkeletalKeyframe>,
) {
    private val split = transform.split(".", "/").toTypedArray()

    private fun SkeletalInstance.getTransform(): TransformInstance {
        var transform = this.transform
        if (split.size == 1 && split[0] == "base") return transform

        for (name in split) {
            transform = transform.children[name] ?: throw IllegalStateException("Animation is referencing unknown transform: $name")
        }
        return transform
    }

    fun instance(instance: SkeletalInstance): KeyframeAnimator {
        val transform = instance.getTransform()

        val instances: Array<KeyframeInstance<*>?> = arrayOfNulls(this.keyframes.size)

        for ((index, keyframe) in this.keyframes.withIndex()) {
            instances[index] = keyframe.instance()
        }

        return KeyframeAnimator(transform, instances.cast())
    }
}
