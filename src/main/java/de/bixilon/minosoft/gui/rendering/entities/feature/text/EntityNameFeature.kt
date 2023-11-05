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

package de.bixilon.minosoft.gui.rendering.entities.feature.text

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer

class EntityNameFeature(renderer: EntityRenderer<*>) : BillboardTextFeature(renderer, null) {

    override fun update(millis: Long, delta: Float) {
        updateName()
        super.update(millis, delta)
    }

    private fun updateName() {
        if (!isNameVisible()) {
            this.text = null
            return
        }
        val name = renderer.entity.name
        if (name == this.text) return
        this.text = name
    }

    private fun isNameVisible(): Boolean {
        if (renderer.entity is PlayerEntity) return true

        val camera = renderer.renderer.connection.camera
        val target = camera.target.target
        if (target !is EntityTarget || target.entity !== renderer.entity) return false

        val distance = camera.entity.nullCast<LocalPlayerEntity>()?.reachDistance ?: 3.0
        if (target.distance > distance) return false
        return true
    }
}
