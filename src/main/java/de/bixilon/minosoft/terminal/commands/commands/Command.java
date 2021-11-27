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

package de.bixilon.minosoft.terminal.commands.commands;

import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.PostChatFormattingCodes;

public abstract class Command {
    private static final String ERROR_MESSAGE_PREFIX = ChatColors.RED.getAnsi();
    private static final String ERROR_MESSAGE_SUFFIX = PostChatFormattingCodes.RESET.getAnsi();

    public static void print(String string, Object... format) {
        if (format.length == 0) {
            System.out.println(string);
            return;
        }
        System.out.printf(string + "%n", format);
    }

    public static void printError(String text, Object... format) {
        System.err.printf(ERROR_MESSAGE_PREFIX + (text) + "%n" + ERROR_MESSAGE_SUFFIX, format);
    }

    public abstract CommandNode build(CommandNode parent);
}
