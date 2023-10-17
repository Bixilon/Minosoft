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

package de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.KeyframeInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types.RotateKeyframe
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types.ScaleKeyframe
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types.TintKeyframe
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types.TranslateKeyframe

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = RotateKeyframe::class, name = RotateKeyframe.TYPE),
    JsonSubTypes.Type(value = TintKeyframe::class, name = TintKeyframe.TYPE),
    JsonSubTypes.Type(value = TranslateKeyframe::class, name = TranslateKeyframe.TYPE),
    JsonSubTypes.Type(value = ScaleKeyframe::class, name = ScaleKeyframe.TYPE),
)

interface SkeletalKeyframe {
    val type: String

    fun instance(): KeyframeInstance
}
