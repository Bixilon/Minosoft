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

package de.bixilon.minosoft.gui.rendering.sound

import de.bixilon.minosoft.modding.event.events.ExplosionEvent
import de.bixilon.minosoft.modding.event.events.PlaySoundEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

object DefaultAudioBehavior {
    private val random = Random()
    private val ENTITY_GENERIC_EXPLODE = "minecraft:entity.generic.explode".toResourceLocation()

    fun register(connection: PlayConnection) {
        val world = connection.world
        val invokers = listOf(
            CallbackEventListener.of<PlaySoundEvent> { world.playSound(it.soundEvent, it.position, it.volume, it.pitch) },
            CallbackEventListener.of<ExplosionEvent> { world.playSound(ENTITY_GENERIC_EXPLODE, it.position, 4.0f, (1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f) * 0.7f) },
        )

        connection.events.register(*invokers.toTypedArray())
    }
}
