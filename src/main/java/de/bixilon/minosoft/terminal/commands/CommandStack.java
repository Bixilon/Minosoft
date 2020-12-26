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

package de.bixilon.minosoft.terminal.commands;

import de.bixilon.minosoft.data.commands.parser.arguments.LiteralArgument;

import java.util.ArrayList;

public class CommandStack {
    private final ArrayList<Object> arguments;
    private final ArrayList<Object> nonLiteralArguments;

    public CommandStack(ArrayList<Object> arguments, ArrayList<Object> nonLiteralArguments) {
        this.arguments = arguments;
        this.nonLiteralArguments = nonLiteralArguments;
    }

    public CommandStack() {
        this.arguments = new ArrayList<>();
        this.nonLiteralArguments = new ArrayList<>();
    }

    public CommandStack(CommandStack stack) {
        this.arguments = new ArrayList<>(stack.arguments);
        this.nonLiteralArguments = new ArrayList<>(stack.nonLiteralArguments);
    }

    public ArrayList<Object> getArguments() {
        return this.arguments;
    }

    public void addArgument(Object argument) {
        this.arguments.add(argument);
        if (!(argument instanceof LiteralArgument)) {
            this.nonLiteralArguments.add(argument);
        }
    }

    public <T> T getArgument(int index) {
        return (T) this.arguments.get(index);
    }

    public <T> T getNonLiteralArgument(int index) {
        return (T) this.nonLiteralArguments.get(index);
    }

    public int getInt(int index) {
        return getNonLiteralArgument(index);
    }
}
