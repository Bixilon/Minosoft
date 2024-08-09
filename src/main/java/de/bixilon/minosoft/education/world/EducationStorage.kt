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

package de.bixilon.minosoft.education.world

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.border.area.StaticBorderArea
import de.bixilon.minosoft.education.MinosoftEducation
import de.bixilon.minosoft.local.storage.MemoryStorage
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class EducationStorage(session: PlaySession) : MemoryStorage(session) {

    override fun loadWorld(world: World) {
        world.border.area = StaticBorderArea(MinosoftEducation.config.world.size.toDouble() * ProtocolDefinition.SECTION_LENGTH)
        world.border.center = Vec2d(0, 0)

        world.dimension = DimensionProperties(minY = 0, height = MinosoftEducation.config.world.height)
    }
}
