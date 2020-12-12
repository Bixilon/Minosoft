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

package de.bixilon.minosoft.data.commands.parser.exceptions;


import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

public class CommandParseException extends Exception {
    private final String errorMessage;
    private final ImprovedStringReader command;
    private final int startIndex;
    private final int endIndex;

    public CommandParseException(String errorMessage, ImprovedStringReader command, int startIndex) {
        this(errorMessage, command, startIndex, null);
    }

    public CommandParseException(String errorMessage, ImprovedStringReader command, int startIndex, Throwable cause) {
        super(String.format("%s  <-- [%s] (%d): %s", command.getString(), command.getString().substring(startIndex), startIndex, errorMessage), cause);
        this.errorMessage = errorMessage;
        this.command = command;
        this.startIndex = startIndex;
        this.endIndex = command.getPosition();
    }

    public CommandParseException(String errorMessage, ImprovedStringReader command, String currentArgument) {
        this(errorMessage, command, currentArgument, null);
    }

    public CommandParseException(String errorMessage, ImprovedStringReader command, String currentArgument, Throwable cause) {
        super(String.format("%s  <-- [%s] (%d): %s", command.getString(), currentArgument, (command.getPosition() - currentArgument.length()), errorMessage), cause);
        this.errorMessage = errorMessage;
        this.command = command;
        this.startIndex = command.getPosition() - currentArgument.length();
        this.endIndex = command.getPosition();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ImprovedStringReader getCommand() {
        return command;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
