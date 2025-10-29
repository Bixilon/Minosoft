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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage

import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.AbstractAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.AnimationResult
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import kotlin.time.Duration

abstract class OpenCloseAnimation(
    protected val instance: SkeletalInstance,
) : AbstractAnimation {
    protected abstract val transform: TransformInstance
    protected var progress = 0.0f
    protected var opening = true

    protected abstract val closingDuration: Duration
    protected abstract val openingDuration: Duration

    override fun draw(delta: Duration): AnimationResult {
        return if (opening) drawOpening(delta) else drawClosing(delta)
    }

    private fun drawOpening(delta: Duration): AnimationResult {
        this.progress += (delta / openingDuration).toFloat()
        if (this.progress > 1.0f) {
            this.progress = 1.0f
        }
        transform()
        return AnimationResult.CONTINUE
    }

    private fun drawClosing(delta: Duration): AnimationResult {
        this.progress -= (delta / closingDuration).toFloat()
        if (progress <= 0.0f) {
            this.progress = 0.0f
            return AnimationResult.ENDED
        }
        transform()
        return AnimationResult.CONTINUE
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
