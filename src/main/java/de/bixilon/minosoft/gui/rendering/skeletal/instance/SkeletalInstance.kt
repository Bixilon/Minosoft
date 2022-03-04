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
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel.Companion.fromBlockCoordinates
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.KeyframeChannels
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY_INSTANCE
import glm_.func.rad
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class SkeletalInstance(
    val renderWindow: RenderWindow,
    blockPosition: Vec3i,
    val model: BakedSkeletalModel,
) {
    private val blockPosition = blockPosition.toVec3
    private var currentAnimation: SkeletalAnimation? = null
    private var animationTime = 0.0f
    private var animationLastFrame = -1L

    fun playAnimation(name: String) {
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
        animationTime = 0.0f
        animationLastFrame = -1L
        this.currentAnimation = animation
    }

    fun draw() {
        renderWindow.renderSystem.reset()
        val shader = renderWindow.shaderManager.skeletalShader
        shader.use()
        setTransforms(shader)

        model.mesh.draw()
    }

    private fun setTransforms(shader: Shader) {
        val base = Mat4().translateAssign(blockPosition.toVec3)

        val origin = Vec3(0, 10, 7).fromBlockCoordinates()
        val lid = Mat4()
        lid.translateAssign(origin)

        val animation = currentAnimation
        if (animation != null) {
            val time = TimeUtil.time
            if (this.animationLastFrame > 0L) {
                val delta = time - this.animationLastFrame
                animationTime += delta / 1000.0f
            }
            animationLastFrame = time

            val rotation = animation.get(KeyframeChannels.ROTATION, animationTime)
            if (rotation != Vec3.EMPTY_INSTANCE) {
                lid.rotateAssign(-rotation.x.rad, Vec3(1, 0, 0))
                lid.rotateAssign(-rotation.y.rad, Vec3(0, 1, 0))
                lid.rotateAssign(-rotation.z.rad, Vec3(0, 0, 1))
            }
            val scale = animation.get(KeyframeChannels.SCALE, animationTime)
            if (scale.x != 1.0f || scale.y != 1.0f || scale.z != 1.0f) {
                lid.scaleAssign(scale)
            }
            val position = animation.get(KeyframeChannels.POSITION, animationTime)
            if (position != Vec3.EMPTY_INSTANCE) {
                // ToDo
            }
        }

        lid.translateAssign(-origin)
        lid[3, 0] += blockPosition.x
        lid[3, 1] += blockPosition.y
        lid[3, 2] += blockPosition.z

        val transforms = arrayOf(base, lid)

        shader["uSkeletalTransforms"] = transforms
    }
}
