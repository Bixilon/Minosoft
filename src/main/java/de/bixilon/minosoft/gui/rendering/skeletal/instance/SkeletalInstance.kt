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

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.DeltaDrawable
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import java.util.*

class SkeletalInstance(
    val renderWindow: RenderWindow,
    val model: BakedSkeletalModel,
    position: Vec3 = Vec3.EMPTY,
    transform: Mat4 = Mat4(),
) {
    private var baseTransform = Mat4().translateAssign(position) * transform
    private var previousBaseTransform = baseTransform
    private var animations: MutableList<SkeletalAnimationInstance> = mutableListOf()
    private var transforms: List<Mat4> = emptyList()

    private var lastDraw = -1L


    var light: Int = 0xFF

    fun getAnimation(name: String): SkeletalAnimation? {
        for (animation in model.model.animations) {
            if (animation.name != name) {
                continue
            }
            return animation
        }
        return null
    }

    fun playAnimation(animation: SkeletalAnimation) {
        val instance = SkeletalAnimationInstance(animation)
        animations.removeAll { it.animation == animation }
        animations += instance
    }

    fun playAnimation(name: String) {
        playAnimation(getAnimation(name) ?: throw IllegalArgumentException("Can not find animation $name"))
    }

    fun clearAnimation() {
        animations.clear()
    }

    fun draw() {
        renderWindow.skeletalManager.draw(this, light)
    }

    fun updatePosition(position: Vec3, rotation: EntityRotation) {
        val matrix = Mat4()
            .translateAssign(position)
            .rotateAssign((180.0f - rotation.yaw).toFloat().rad, Vec3(0, 1, 0))
            .translateAssign(Vec3(-0.5, 0, -0.5)) // move to center

        if (baseTransform != matrix) {
            baseTransform = matrix
        }
    }

    fun calculateTransforms(): List<Mat4> {
        val baseTransform = baseTransform
        val time = TimeUtil.millis
        if (animations.isNotEmpty()) {
            val toRemove: MutableSet<SkeletalAnimationInstance> = mutableSetOf()
            for (animation in animations) {
                animation.draw(time)
                if (animation.canClear()) {
                    toRemove += animation
                }
            }
            if (toRemove.isNotEmpty()) {
                this.animations -= toRemove
            }
        }
        if (animations.isEmpty()) {
            if (this.transforms.isNotEmpty() && baseTransform === previousBaseTransform) {
                return this.transforms
            }
        }

        val delta = time - lastDraw
        for (instance in animations) {
            val animation = instance.animation
            if (animation is DeltaDrawable) {
                animation.draw(delta)
            }
        }
        val transforms: MutableList<Mat4> = mutableListOf()
        for (outliner in model.model.outliner) {
            calculateTransform(baseTransform, animations, outliner, transforms)
        }
        this.transforms = transforms
        this.previousBaseTransform = baseTransform
        return transforms
    }

    private fun calculateTransform(transform: Mat4, animations: List<SkeletalAnimationInstance>, outliner: Any /* UUID or SkeletalOutliner */, transforms: MutableList<Mat4>) {
        if (outliner is UUID) {
            return
        }
        check(outliner is SkeletalOutliner)
        val skeletalTransform = Mat4(transform)

        for (animation in this.animations) {
            skeletalTransform *= animation.animation.calculateTransform(outliner, animation.time)
        }

        transforms += skeletalTransform

        for (child in outliner.children) {
            calculateTransform(skeletalTransform, animations, child, transforms)
        }
    }

    fun unload() {
        model.unload()
    }
}
