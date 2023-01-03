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
package de.bixilon.minosoft.data.entities.entities.monster

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.animal.AbstractGolem
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class Shulker(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractGolem(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val attachmentFace: Directions
        get() = data.get(ATTACH_FACE_DATA, Directions.NORTH)

    @get:SynchronizedEntityData
    val attachmentPosition: Vec3i?
        get() = data.get(ATTACH_POSITION_DATA, null)

    @get:SynchronizedEntityData
    val peek: Byte
        get() = data.get(PEEK_DATA, 0x00.toByte())

    @get:SynchronizedEntityData
    val color: RGBColor
        get() = ChatColors.VALUES.getOrNull(data.get(COLOR_DATA, 0x00)) ?: ChatColors.DARK_PURPLE


    companion object : EntityFactory<Shulker> {
        override val identifier: ResourceLocation = KUtil.minecraft("shulker")
        private val ATTACH_FACE_DATA = EntityDataField("SHULKER_ATTACH_FACE")
        private val ATTACH_POSITION_DATA = EntityDataField("SHULKER_ATTACHMENT_POSITION")
        private val PEEK_DATA = EntityDataField("SHULKER_PEEK")
        private val COLOR_DATA = EntityDataField("SHULKER_COLOR")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Shulker {
            return Shulker(connection, entityType, data, position, rotation)
        }
    }
}
