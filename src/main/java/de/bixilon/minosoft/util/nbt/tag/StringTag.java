/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.nbt.tag;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;

public class StringTag extends NBTTag {
    private final String value;

    public StringTag(String value) {
        this.value = value;
    }

    public StringTag(InByteBuffer buffer) {
        this.value = buffer.readString(new ShortTag(buffer).getValue());
    }

    @Override
    public TagTypes getType() {
        return TagTypes.STRING;
    }

    @Override
    public void writeBytes(OutByteBuffer buffer) {
        buffer.writeShort((short) this.value.length());
        buffer.writeStringNoLength(this.value);
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return String.format("\"%s\"", this.value);
    }
}
