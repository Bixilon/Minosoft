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

package de.bixilon.minosoft.gui.rendering.entities.feature.text.name

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.Mob
import de.bixilon.minosoft.data.entities.entities.decoration.ItemFrame
import de.bixilon.minosoft.data.entities.entities.decoration.armorstand.ArmorStand
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.scoreboard.NameTagVisibilities
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.entities.feature.properties.InvisibleFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.text.BillboardTextFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer

class EntityNameFeature(renderer: EntityRenderer<*>) : BillboardTextFeature(renderer, null), InvisibleFeature {
    private var delta = 0.0f
    override val renderInvisible get() = true

    init {
        renderer.entity.data.observe<ChatComponent?>(Entity.CUSTOM_NAME_DATA) { delta = 0.0f }
    }

    override fun update(millis: Long, delta: Float) {
        this.delta += delta
        if (this.delta >= UPDATE_INTERVAL) {
            updateName()
            this.delta = 0.0f
        }
        super.update(millis, delta)
    }

    private fun updateName() {
        this.text = getEntityName()
    }

    private fun Entity.getName(invisible: Boolean): ChatComponent? {
        if (invisible) return null
        if (this.isNameVisible) {
            var name = this.name
            if (name == null && this is LivingEntity) name = connection.language.translate(type.translationKey) // TODO: Is that correct?
            return name
        }
        if (!isTargeted()) return null
        return name
    }

    private fun LivingEntity.getName(invisible: Boolean): ChatComponent? {
        if (this.primaryPassenger != null) return null

        val renderer = this@EntityNameFeature.renderer.renderer
        val profile = renderer.profile.features.name
        if (this === renderer.connection.camera.entity && (!renderer.context.camera.view.view.renderSelf || !profile.local)) return null
        if (!this.isNameVisible) return null

        if (invisible) return null

        return this.name
    }

    private fun ArmorStand.getName(): ChatComponent? {
        if (!isNameVisible) return null

        return customName
    }

    private fun ItemFrame.getName(): ChatComponent? {
        if (!isTargeted()) return null
        val item = this.item ?: return null

        return item._display?._customDisplayName
    }

    private fun getEntityName(): ChatComponent? {
        val profile = renderer.renderer.profile.features.name
        if (!profile.enabled) return null

        val entity = renderer.entity

        val distance = if (entity is LivingEntity && entity.pose == Poses.SNEAKING) SNEAKING_DISTANCE * SNEAKING_DISTANCE else RENDER_DISTANCE * RENDER_DISTANCE
        if (renderer.distance >= distance) return null

        val invisible = isInvisible()

        return when (renderer.entity) {
            is ItemFrame -> renderer.entity.getName()
            is ArmorStand -> renderer.entity.getName()
            is Mob -> renderer.entity.unsafeCast<Entity>().getName(invisible)
            is LivingEntity -> renderer.entity.getName(invisible)
            else -> renderer.entity.getName(invisible)
        }
    }

    private fun isInvisible(): Boolean {
        val camera = renderer.renderer.connection.camera.entity
        val entity = renderer.entity
        val invisible = entity.isInvisible(camera)

        if (entity !is PlayerEntity) return invisible
        val team = entity.additional.team ?: return invisible
        val name = team.visibility.name

        when (name) {
            NameTagVisibilities.ALWAYS -> return invisible
            NameTagVisibilities.NEVER -> return true
            else -> Unit
        }

        val cTeam = camera.nullCast<PlayerEntity>()?.additional?.team ?: return invisible
        val sameTeam = team.name == cTeam.name


        return when (name) {
            NameTagVisibilities.HIDE_FOR_ENEMIES -> !sameTeam || (team.visibility.invisibleTeam && invisible)
            NameTagVisibilities.HIDE_FOR_MATES -> sameTeam || invisible
            else -> Broken()
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
        const val UPDATE_INTERVAL = 0.2f
    }
}
