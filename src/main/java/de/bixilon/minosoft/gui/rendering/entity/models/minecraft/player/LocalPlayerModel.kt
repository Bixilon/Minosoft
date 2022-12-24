/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer

open class LocalPlayerModel(renderer: EntityRenderer, player: PlayerEntity) : PlayerModel(renderer, player) {

    override val hideSkeletalModel: Boolean
        get() = super.hideSkeletalModel || !context.camera.view.view.renderSelf

    init {
        renderer.profile.hitbox::showLocal.observe(this, true) {
            hitbox.enabled = it
        }
    }
}
