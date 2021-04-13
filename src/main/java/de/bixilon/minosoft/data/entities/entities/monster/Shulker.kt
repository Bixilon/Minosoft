/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.entities.entities.animal.AbstractGolem
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class Shulker(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : AbstractGolem(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Attachment face")
    val attachmentFace: Directions
        get() = entityMetaData.sets.getDirection(EntityMetaDataFields.SHULKER_ATTACH_FACE)

    @get:EntityMetaDataFunction(name = "Attachment position")
    val attachmentPosition: Vec3i?
        get() = entityMetaData.sets.getBlockPosition(EntityMetaDataFields.SHULKER_ATTACHMENT_POSITION)

    @get:EntityMetaDataFunction(name = "Peek")
    val peek: Byte
        get() = entityMetaData.sets.getByte(EntityMetaDataFields.SHULKER_PEEK)

    @get:EntityMetaDataFunction(name = "Color")
    val color: RGBColor
        get() = ChatColors.getColorById(entityMetaData.sets.getByte(EntityMetaDataFields.SHULKER_COLOR).toInt())


    companion object : EntityFactory<Shulker> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("shulker")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation): Shulker {
            return Shulker(connection, entityType, position, rotation)
        }
    }
}
