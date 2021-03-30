/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.entities.monster;

import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.animal.AbstractGolem;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;
import de.bixilon.minosoft.protocol.network.Connection;
import glm_.vec3.Vec3;
import glm_.vec3.Vec3i;

import javax.annotation.Nullable;
import java.util.UUID;

public class Shulker extends AbstractGolem {

    public Shulker(Connection connection, int entityId, UUID uuid, Vec3 position, EntityRotation rotation) {
        super(connection, entityId, uuid, position, rotation);
    }

    @EntityMetaDataFunction(name = "Attachment face")
    public Directions getAttachmentFace() {
        return this.metaData.getSets().getDirection(EntityMetaDataFields.SHULKER_ATTACH_FACE);
    }

    @EntityMetaDataFunction(name = "Attachment position")
    @Nullable
    public Vec3i getAttachmentPosition() {
        return this.metaData.getSets().getBlockPosition(EntityMetaDataFields.SHULKER_ATTACHMENT_POSITION);
    }

    @EntityMetaDataFunction(name = "Peek")
    public byte getPeek() {
        return this.metaData.getSets().getByte(EntityMetaDataFields.SHULKER_PEEK);
    }

    @EntityMetaDataFunction(name = "Color")
    public RGBColor getColor() {
        return ChatColors.getColorById(this.metaData.getSets().getByte(EntityMetaDataFields.SHULKER_COLOR));
    }
}
