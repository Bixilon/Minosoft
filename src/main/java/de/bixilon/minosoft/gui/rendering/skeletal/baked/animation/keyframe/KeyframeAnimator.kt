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

package de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe

import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.NOT_OVER
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.OVER
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance

class KeyframeAnimator(
    val transform: TransformInstance,
    val keyframes: Array<KeyframeInstance<*>>,
) {

    fun draw(time: Float): Boolean {
        var over = OVER

        for (frame in keyframes) {
            if (frame.transform(time, transform) == NOT_OVER) {
                over = NOT_OVER
            }
        }

        return over
    }
}
