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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage

import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.AbstractAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.NOT_OVER
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.OVER
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance

abstract class OpenCloseAnimation(
    protected val instance: SkeletalInstance,
) : AbstractAnimation {
    protected abstract val transform: TransformInstance
    protected var progress = 0.0f
    protected var opening = true

    protected abstract val closingDuration: Float
    protected abstract val openingDuration: Float

    override fun draw(delta: Float): Boolean {
        return if (opening) drawOpening(delta) else drawClosing(delta)
    }

    private fun drawOpening(delta: Float): Boolean {
        this.progress += delta / openingDuration
        if (this.progress > 1.0f) {
            this.progress = 1.0f
        }
        transform()
        return NOT_OVER
    }

    private fun drawClosing(delta: Float): Boolean {
        this.progress -= delta / closingDuration
        if (progress <= 0.0f) {
            this.progress = 0.0f
            return OVER
        }
        transform()
        return NOT_OVER
    }

    protected abstract fun transform()

    fun open() {
        if (progress <= 0.0f) {
            instance.animation.play(this)
        }
        opening = true
    }

    fun close() {
        opening = false
    }
}
