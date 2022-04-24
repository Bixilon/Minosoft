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
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.animal.AbstractGolem
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Shulker(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : AbstractGolem(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData(name = "Attachment face")
    val attachmentFace: Directions
        get() = data.sets.getDirection(EntityDataFields.SHULKER_ATTACH_FACE)

    @get:SynchronizedEntityData(name = "Attachment position")
    val attachmentPosition: Vec3i?
        get() = data.sets.getBlockPosition(EntityDataFields.SHULKER_ATTACHMENT_POSITION)

    @get:SynchronizedEntityData(name = "Peek")
    val peek: Byte
        get() = data.sets.getByte(EntityDataFields.SHULKER_PEEK)

    @get:SynchronizedEntityData(name = "Color")
    val color: RGBColor
        get() = ChatColors[data.sets.getByte(EntityDataFields.SHULKER_COLOR).toInt()]


    companion object : EntityFactory<Shulker> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("shulker")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Shulker {
            return Shulker(connection, entityType, data, position, rotation)
        }
    }
}
