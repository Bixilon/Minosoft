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

package de.bixilon.minosoft.data.text;

import de.bixilon.minosoft.util.hash.BetterHashSet;

public class MultiChatComponent extends TextComponent {
    private ClickEvent clickEvent;
    private HoverEvent hoverEvent;

    public MultiChatComponent(String text, RGBColor color, BetterHashSet<ChatFormattingCode> formatting, ClickEvent clickEvent, HoverEvent hoverEvent) {
        super(text, color, formatting);
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
    }

    public MultiChatComponent(String text, RGBColor color, BetterHashSet<ChatFormattingCode> formatting) {
        super(text, color, formatting);
    }

    public MultiChatComponent(String text, RGBColor color) {
        super(text, color);
    }

    public MultiChatComponent(String text) {
        super(text);
    }

    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    public void setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    public void setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
    }
}
