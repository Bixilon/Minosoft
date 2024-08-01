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

package de.bixilon.minosoft.camera

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.minosoft.camera.target.TargetHandler
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.input.interaction.InteractionManager
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class SessionCamera(
    val session: PlaySession,
) {
    var entity: Entity by observed(null).unsafeCast<DataObserver<Entity>>()
    val target = TargetHandler(this)
    val interactions: InteractionManager = unsafeNull()

    fun init() {
        entity = session.player
        INTERACTIONS[this] = InteractionManager(this)
        interactions.init()
    }


    companion object {
        private val INTERACTIONS = SessionCamera::interactions.jvmField
    }
}
