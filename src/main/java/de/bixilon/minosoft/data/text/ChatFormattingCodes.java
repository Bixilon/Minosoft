/*
 * Minosoft
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

package de.bixilon.minosoft.data.text;

import com.google.common.collect.HashBiMap;

import java.util.Arrays;

public enum ChatFormattingCodes {
    OBFUSCATED('k', "\u001b[5m"),
    BOLD('l', "\u001b[1m"),
    STRIKETHROUGH('m', "\u001b[9m"),
    UNDERLINED('n', "\u001b[4m"),
    ITALIC('o', "\u001b[3m"),
    RESET('r', "\u001b[0m", ChatFormattingCodePosition.POST);

    private final static HashBiMap<Character, ChatFormattingCodes> formattingCodes = HashBiMap.create();

    static {
        Arrays.stream(values()).forEach(chatFormattingCodes -> formattingCodes.put(chatFormattingCodes.getChar(), chatFormattingCodes));
    }

    final char c;
    final String ansi;
    final ChatFormattingCodePosition position;

    ChatFormattingCodes(char c, String ansi) {
        this.c = c;
        this.ansi = ansi;
        this.position = ChatFormattingCodePosition.PRE;
    }

    ChatFormattingCodes(char c, String ansi, ChatFormattingCodePosition position) {
        this.c = c;
        this.ansi = ansi;
        this.position = position;
    }

    public static ChatFormattingCodes getChatFormattingCodeByChar(char c) {
        return formattingCodes.get(c);
    }

    public char getChar() {
        return c;
    }

    public ChatFormattingCodePosition getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return getANSI();
    }

    public String getANSI() {
        return ansi;
    }

    public enum ChatFormattingCodePosition {
        PRE,
        POST
    }
}

