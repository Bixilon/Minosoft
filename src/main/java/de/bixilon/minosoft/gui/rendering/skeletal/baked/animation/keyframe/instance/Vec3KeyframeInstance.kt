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

package de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.KeyframeInterpolation
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.interpolateLinear
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.interpolateSine
import java.util.*
import kotlin.time.Duration

abstract class Vec3KeyframeInstance(
    data: TreeMap<Duration, Vec3f>,
    loop: AnimationLoops,
    val interpolation: KeyframeInterpolation,
) : KeyframeInstance<Vec3f>(data, loop) {


    override fun interpolate(delta: Float, previous: Vec3f, next: Vec3f) = when (interpolation) {
        KeyframeInterpolation.NONE -> if (delta >= 1.0f) next else previous
        KeyframeInterpolation.LINEAR -> interpolateLinear(delta, previous, next)
        KeyframeInterpolation.SINE -> interpolateSine(delta, previous, next)
    }
}
