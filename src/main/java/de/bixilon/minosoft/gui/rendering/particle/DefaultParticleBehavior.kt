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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfile
import de.bixilon.minosoft.gui.rendering.particle.types.norender.ExplosionEmitterParticle
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.explosion.ExplosionParticle
import de.bixilon.minosoft.modding.event.events.ExplosionEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

object DefaultParticleBehavior {

    fun register(session: PlaySession, renderer: ParticleRenderer) {
        val profile = session.profiles.particle
        session.explosion(renderer, profile)
    }

    private fun PlaySession.explosion(renderer: ParticleRenderer, profile: ParticleProfile) {
        val explosion = registries.particleType[ExplosionParticle] ?: return
        val emitter = registries.particleType[ExplosionEmitterParticle] ?: return

        this.events.listen<ExplosionEvent> {
            if (!profile.types.explosions) {
                return@listen
            }
            if (it.power >= 2.0f) {
                renderer += ExplosionEmitterParticle(this, it.position, emitter.default())
            } else {
                renderer += ExplosionParticle(this, it.position, explosion.default())
            }
        }
    }
}
