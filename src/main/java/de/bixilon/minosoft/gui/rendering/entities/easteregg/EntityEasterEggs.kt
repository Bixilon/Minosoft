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

package de.bixilon.minosoft.gui.rendering.entities.easteregg

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.SkinParts

object EntityEasterEggs {
    private val FLIPPED = setOf("Dinnerbone", "Grumm")
    val FLIP_ROTATION = Vec3(0, 0.0f, 180.0f.rad)
    const val FLIP_ENABLED = true

    fun Entity.isFlipped(): Boolean {
        if (!FLIP_ENABLED) return false
        var name = this.customName?.message
        if (name == null && this is PlayerEntity) {
            name = additional.name
        }
        if (name == null) return false
        when (name) {
            "Dinnerbone", "Grumm" -> Unit
            else -> return false
        }
        if (this is PlayerEntity) return SkinParts.CAPE in this.skinParts
        return true
    }
}
