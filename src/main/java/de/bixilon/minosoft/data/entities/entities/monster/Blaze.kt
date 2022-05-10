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
package de.bixilon.minosoft.data.entities.entities.monster

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Blaze(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Monster(connection, entityType, data, position, rotation) {

    private fun getBlazeFlag(bitMask: Int): Boolean {
        return data.getBitMask(BLAZE_DATA, bitMask, 0x00)
    }

    @get:SynchronizedEntityData
    val isBurning: Boolean
        get() = getBlazeFlag(0x01)

    override val hitBoxColor: RGBColor
        get() = RGBColor(0xFFA500FF.toInt())

    companion object : EntityFactory<Blaze> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("blaze")
        private val BLAZE_DATA = EntityDataField("BLAZE_FLAGS")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Blaze {
            return Blaze(connection, entityType, data, position, rotation)
        }
    }
}
