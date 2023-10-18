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

package de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.KeyframeInterpolation
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateSine

abstract class Vec3KeyframeInstance(
    data: Map<Float, Vec3>,
    loop: AnimationLoops,
    val interpolation: KeyframeInterpolation,
) : KeyframeInstance<Vec3>(data, loop) {


    override fun interpolate(delta: Float, previous: Vec3, next: Vec3) = when (interpolation) {
        KeyframeInterpolation.NONE -> if (delta >= 1.0f) next else previous
        KeyframeInterpolation.LINEAR -> interpolateLinear(delta, previous, next)
        KeyframeInterpolation.SINE -> interpolateSine(delta, previous, next)
    }
}
