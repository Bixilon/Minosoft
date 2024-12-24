/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.education.MinosoftEducation

class SpectateHandler(
    private val interactions: InteractionManager,
) {
    private val session = interactions.session
    private val rateLimiter = RateLimiter()

    fun init() {
        session.camera::entity.observe(this) { spectate(it) }
        session.player.additional::gamemode.observe(this) { spectate(interactions.camera.entity) }
    }

    // TODO: stop spectating (simulate sneak)


    fun spectate(entity: Entity?) {
        if (!MinosoftEducation.config.features.spectating) return
        var entity = entity ?: session.player
        if (session.player.gamemode != Gamemodes.SPECTATOR) {
            entity = session.player
        }
        interactions.camera.entity = entity
    }

    fun draw() {
        rateLimiter.work()
    }
}
