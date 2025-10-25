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

package de.bixilon.minosoft.local.storage

import de.bixilon.kmath.vec.vec2.d.Vec2d
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.border.area.StaticBorderArea
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.math.sqrt

class DebugStorage(session: PlaySession) : MemoryStorage(session) {

    override fun load(world: World) {
        val size = (sqrt(session.registries.blockState.size.toFloat())).toInt() + 1

        world.border.area = StaticBorderArea(size.toDouble())
        world.border.center = Vec2d(size)
    }
}
