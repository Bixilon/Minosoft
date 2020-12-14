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

package de.bixilon.minosoft.data.entities.block;

import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;
import de.bixilon.minosoft.util.nbt.tag.IntTag;
import de.bixilon.minosoft.util.nbt.tag.NBTTag;
import de.bixilon.minosoft.util.nbt.tag.StringTag;

public class BedEntityMetaData extends BlockEntityMetaData {
    private final RGBColor color;

    public BedEntityMetaData(RGBColor color) {
        this.color = color;
    }

    public BedEntityMetaData(NBTTag nbt) {
        if (nbt == null) {
            this.color = ChatColors.RED;
            return;
        }
        if (nbt instanceof StringTag stringTag) {
            // yes, we support bed rgb colors :D
            this.color = new RGBColor(stringTag.getValue());
            return;
        }
        this.color = switch (((IntTag) nbt).getValue()) {
            case 0 -> new RGBColor(255, 255, 255); // white
            case 1 -> new RGBColor(234, 103, 3); // orange
            case 2 -> new RGBColor(199, 78, 189); // magenta
            case 3 -> new RGBColor(47, 162, 212); // light blue
            case 4 -> new RGBColor(251, 194, 32); // yellow
            case 5 -> new RGBColor(101, 178, 24); // lime
            case 6 -> new RGBColor(236, 126, 161); // pink
            case 7 -> new RGBColor(76, 76, 76); // gray
            case 8 -> new RGBColor(130, 130, 120); // light gray
            case 9 -> new RGBColor(22, 128, 142); // cyan
            case 10 -> new RGBColor(99, 30, 154); // purple
            case 11 -> new RGBColor(44, 46, 143); // blue
            case 12 -> new RGBColor(105, 65, 35); // brown
            case 13 -> new RGBColor(77, 97, 34); // green
            case 14 -> new RGBColor(139, 30, 31); // red
            case 15 -> new RGBColor(15, 16, 19); // black
            default -> throw new IllegalStateException("Unexpected value: " + ((IntTag) nbt).getValue());
        };
    }

    public RGBColor getColor() {
        return this.color;
    }
}
