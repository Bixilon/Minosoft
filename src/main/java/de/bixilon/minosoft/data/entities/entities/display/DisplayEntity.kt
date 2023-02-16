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
package de.bixilon.minosoft.data.entities.entities.display

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class DisplayEntity(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    private companion object {
        private val INTERPOLATION_START = EntityDataField("INTERPOLATION_START")
        private val INTERPOLATION_DURATION = EntityDataField("INTERPOLATION_DURATION")
        private val TRANSLATION = EntityDataField("TRANSLATION")
        private val SCALE = EntityDataField("SCALE")
        private val LEFT_ROTATION = EntityDataField("LEFT_ROTATION")
        private val RIGHT_ROTATION = EntityDataField("RIGHT_ROTATION")
        private val BRIGHTNESS = EntityDataField("BRIGHTNESS")
        private val VIEW_RANGE = EntityDataField("VIEW_RANGE")
        private val SHADOW_RADIUS = EntityDataField("SHADOW_RADIUS")
        private val SHADOW_STRENGTH = EntityDataField("SHADOW_STRENGTH")
        private val WIDTH = EntityDataField("WIDTH")
        private val HEIGHT = EntityDataField("HEIGHT")
        private val GLOW_COLOR_OVERRIDE = EntityDataField("GLOW_COLOR_OVERRIDE")
    }
}
