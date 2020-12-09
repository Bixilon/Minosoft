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

package de.bixilon.minosoft.data.commands.parser.properties;

import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;

public class FloatParserProperties implements ParserProperties {
    private final float minValue;
    private final float maxValue;

    public FloatParserProperties(InByteBuffer buffer) {
        byte flags = buffer.readByte();
        if (BitByte.isBitMask(flags, 0x01)) {
            minValue = buffer.readFloat();
        } else {
            minValue = Float.MIN_VALUE;
        }
        if (BitByte.isBitMask(flags, 0x02)) {
            maxValue = buffer.readFloat();
        } else {
            maxValue = Float.MAX_VALUE;
        }
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }
}
