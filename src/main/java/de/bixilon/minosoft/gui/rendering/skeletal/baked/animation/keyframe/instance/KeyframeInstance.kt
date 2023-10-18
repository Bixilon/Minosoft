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

import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops

abstract class KeyframeInstance<T>(
    private val data: Map<Float, T>,
    private val loop: AnimationLoops,
) {
    private var current: T? = null
    private var currentTime = 0.0f
    private var next: T? = null
    private var nextTime = 0.0f


    abstract fun interpolate(delta: Float, previous: T, next: T): T
    abstract fun apply(value: T, transform: TransformInstance)

    fun transform(time: Float, transform: TransformInstance): Boolean {
        TODO()
    }
}
