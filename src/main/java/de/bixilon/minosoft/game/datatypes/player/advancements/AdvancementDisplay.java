/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.player.advancements;

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.util.BitByte;

public class AdvancementDisplay {
    final TextComponent title;
    final TextComponent description;
    final Slot icon;
    final AdvancementFrameTypes frameType;
    final int flags;
    final String backgroundTexture;
    final float x;
    final float y;

    public AdvancementDisplay(TextComponent title, TextComponent description, Slot icon, AdvancementFrameTypes frameType, int flags, String backgroundTexture, float x, float y) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.frameType = frameType;
        this.flags = flags;
        this.backgroundTexture = backgroundTexture;
        this.x = x;
        this.y = y;
    }

    public AdvancementDisplay(TextComponent title, TextComponent description, Slot icon, AdvancementFrameTypes frameType, int flags, float x, float y) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.frameType = frameType;
        this.flags = flags;
        this.backgroundTexture = null;
        this.x = x;
        this.y = y;
    }

    public TextComponent getTitle() {
        return title;
    }

    public TextComponent getDescription() {
        return description;
    }

    public Slot getIcon() {
        return icon;
    }

    public AdvancementFrameTypes getFrameType() {
        return frameType;
    }

    public boolean hasBackgroundTexture() {
        return BitByte.isBitMask(flags, 0x01);
    }

    public boolean showToast() {
        return BitByte.isBitMask(flags, 0x02);
    }

    public boolean isHidden() {
        return BitByte.isBitMask(flags, 0x04);
    }

    public String getBackgroundTexture() {
        return backgroundTexture;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public enum AdvancementFrameTypes {
        TASK,
        CHALLENGE,
        GOAL;

        public static AdvancementFrameTypes byId(int id) {
            return values()[id];
        }
    }
}
