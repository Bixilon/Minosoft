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
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.Mob
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer

class EntityNameFeature(renderer: EntityRenderer<*>) : BillboardTextFeature(renderer, null) {

    override fun update(millis: Long, delta: Float) {
        updateName()
        super.update(millis, delta)
    }

    private fun updateName() {
        this.text = getEntityName()
    }

    private fun Entity.getName(): ChatComponent? {
        if (this.isNameVisible) return name
        if (!isTargeted()) return null
        return name
    }

    private fun LivingEntity.getName(): ChatComponent? {
        val distance = if (this.pose == Poses.SNEAKING) SNEAKING_DISTANCE * SNEAKING_DISTANCE else RENDER_DISTANCE * RENDER_DISTANCE
        if (this@EntityNameFeature.renderer.distance >= distance) return null
        if (this.primaryPassenger != null) return null

        val renderer = this@EntityNameFeature.renderer.renderer
        val profile = renderer.profile.features.name
        if (this === renderer.connection.camera.entity && (!renderer.context.camera.view.view.renderSelf || !profile.local)) return null

        // TODO: invisibility (w/ teams)

        return this.name
    }

    private fun getEntityName(): ChatComponent? {
        return when (renderer.entity) {
            is Mob -> renderer.entity.unsafeCast<Entity>().getName()
            is LivingEntity -> renderer.entity.getName()
            else -> renderer.entity.getName()
        }
    }

    private fun isTargeted(): Boolean {
        val camera = renderer.renderer.connection.camera
        val target = camera.target.target
        if (target !is EntityTarget || target.entity !== renderer.entity) return false

        val distance = camera.entity.nullCast<LocalPlayerEntity>()?.reachDistance ?: 3.0
        if (target.distance > distance) return false
        return true
    }

    companion object {
        const val SNEAKING_DISTANCE = 32
    }
}
