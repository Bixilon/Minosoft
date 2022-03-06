/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.skeletal.instance

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3i
import java.util.*

class SkeletalInstance(
    val renderWindow: RenderWindow,
    blockPosition: Vec3i,
    val model: BakedSkeletalModel,
    transform: Mat4 = Mat4(),
) {
    private var baseTransform = Mat4().translateAssign(blockPosition.toVec3) * transform
    private var currentAnimation: SkeletalAnimation? = null
    private var animationTime = 0.0f
    private var animationLastFrame = -1L
    private var transforms: List<Mat4> = listOf()

    fun playAnimation(name: String) {
        clearAnimation()
        var animation: SkeletalAnimation? = null
        for (animationEntry in model.model.animations) {
            if (animationEntry.name != name) {
                continue
            }
            animation = animationEntry
            break
        }
        if (animation == null) {
            throw IllegalArgumentException("Can not find animation $name")
        }
        this.currentAnimation = animation
    }

    fun clearAnimation() {
        animationTime = 0.0f
        animationLastFrame = -1L
        this.currentAnimation = null
    }

    fun draw() {
        renderWindow.skeletalManager.draw(this)
    }

    fun calculateTransforms(): List<Mat4> {
        val animation = currentAnimation
        if (animation != null) {
            val time = TimeUtil.time
            if (this.animationLastFrame > 0L) {
                val delta = time - this.animationLastFrame
                animationTime += delta / 1000.0f
            }
            animationLastFrame = time
            if (animation.canClear(animationTime)) {
                clearAnimation()
                return calculateTransforms()
            }
        } else if (this.transforms.isNotEmpty()) {
            return this.transforms
        }

        val transforms: MutableList<Mat4> = mutableListOf()
        for (outliner in model.model.outliner) {
            calculateTransform(animationTime, baseTransform, animation, outliner, transforms)
        }
        this.transforms = transforms
        return transforms
    }

    private fun calculateTransform(animationTime: Float, transform: Mat4, animation: SkeletalAnimation?, outliner: Any /* UUID or SkeletalOutliner */, transforms: MutableList<Mat4>) {
        if (outliner is UUID) {
            return
        }
        check(outliner is SkeletalOutliner)
        val skeletalTransform = transform * (animation?.calculateTransform(outliner, animationTime) ?: Mat4())

        transforms += skeletalTransform

        for (child in outliner.children) {
            calculateTransform(animationTime, skeletalTransform, animation, child, transforms)
        }
    }
}
