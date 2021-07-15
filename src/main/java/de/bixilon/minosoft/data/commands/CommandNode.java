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

package de.bixilon.minosoft.data.commands;

import com.google.errorprone.annotations.DoNotCall;
import de.bixilon.minosoft.data.commands.parser.arguments.LiteralArgument;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.NoConnectionCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.RequiresMoreArgumentsCommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.WrongArgumentCommandParseException;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.terminal.commands.CommandStack;
import de.bixilon.minosoft.terminal.commands.exceptions.CLIException;
import de.bixilon.minosoft.terminal.commands.executors.CommandConnectionExecutor;
import de.bixilon.minosoft.terminal.commands.executors.CommandExecutor;
import de.bixilon.minosoft.util.BitByte;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class CommandNode {
    protected final boolean isExecutable;
    protected final HashSet<CommandArgumentNode> argumentsChildren = new HashSet<>();
    protected final HashMap<String, CommandLiteralNode> literalChildren = new HashMap<>();
    protected final int redirectNodeId;
    protected final Object executor;
    protected int[] childrenIds;
    protected CommandNode redirectNode;

    public CommandNode(int flags, InByteBuffer buffer) {
        this.isExecutable = BitByte.isBitMask(flags, 0x04);
        this.childrenIds = buffer.readVarIntArray(buffer.readVarInt());
        if (BitByte.isBitMask(flags, 0x08)) {
            this.redirectNodeId = buffer.readVarInt();
        } else {
            this.redirectNodeId = -1;
        }
        this.executor = null;
    }


    public CommandNode(CommandNode... children) {
        this(false, null, children);
    }

    public CommandNode(CommandExecutor executor, CommandNode... children) {
        this(true, executor, children);
    }

    public CommandNode(CommandConnectionExecutor executor, CommandNode... children) {
        this(true, executor, children);
    }

    private CommandNode(boolean isExecutable, Object executor, CommandNode... children) {
        this.isExecutable = isExecutable;
        addChildren(children);
        this.executor = executor;
        this.childrenIds = null;
        this.redirectNodeId = -1;
    }

    public Object getExecutor() {
        return this.executor;
    }

    public boolean isExecutable() {
        return this.isExecutable;
    }

    public HashMap<String, CommandLiteralNode> getLiteralChildren() {
        return this.literalChildren;
    }

    public HashSet<CommandArgumentNode> getArgumentsChildren() {
        return this.argumentsChildren;
    }

    public CommandNode getRedirectNode() {
        return this.redirectNode;
    }

    @DoNotCall
    public void setRedirectNode(CommandNode redirectNode) {
        if (this.redirectNode != null) {
            throw new IllegalArgumentException("Object already initialized!");
        }
        this.redirectNode = redirectNode;
    }

    @DoNotCall
    public int getRedirectNodeId() {
        return this.redirectNodeId;
    }

    @DoNotCall
    public int[] getChildrenIds() {
        return this.childrenIds;
    }

    protected CommandStack parse(PlayConnection connection, CommandStringReader stringReader, CommandStack stack, boolean execute) throws CommandParseException, CLIException {
        stringReader.skipWhitespaces();
        if (stringReader.getRemainingLength() == 0) {
            if (this.isExecutable) {
                if (this.executor != null) {
                    if (this.executor instanceof CommandExecutor) {
                        ((CommandExecutor) this.executor).execute(stack);
                    } else if (this.executor instanceof CommandConnectionExecutor) {
                        if (connection == null) {
                            throw new NoConnectionCommandParseException(stringReader, stringReader.getString());
                        }
                        ((CommandConnectionExecutor) this.executor).execute(connection, stack);
                    }
                }

                return stack;
            }
            throw new RequiresMoreArgumentsCommandParseException(stringReader);
        }
        String nextArgument = stringReader.readUnquotedString();
        if (this.literalChildren.containsKey(nextArgument)) {
            stack.addArgument(new LiteralArgument(nextArgument));
            return this.literalChildren.get(nextArgument).parse(connection, stringReader, stack, execute);
        }
        stringReader.skip(-nextArgument.length());
        CommandParseException lastException = null;
        for (CommandArgumentNode argumentNode : this.argumentsChildren) {
            int currentPosition = stringReader.getCursor();
            CommandStack newStack = new CommandStack(stack);
            try {
                return argumentNode.parse(connection, stringReader, newStack, execute);
            } catch (CommandParseException e) {
                lastException = e;
            }
            stringReader.setCursor(currentPosition);
        }
        if (lastException != null) {
            throw lastException;
        }
        stringReader.skip(nextArgument.length());
        throw new WrongArgumentCommandParseException(stringReader, nextArgument);
    }

    public CommandStack parse(PlayConnection connection, CommandStringReader stringReader, CommandStack stack) throws CommandParseException, CLIException {
        return parse(connection, stringReader, stack, false);
    }

    public CommandStack execute(PlayConnection connection, CommandStringReader stringReader, CommandStack stack) throws CommandParseException, CLIException {
        return parse(connection, stringReader, stack, true);
    }

    public CommandStack parse(PlayConnection connection, String string) throws CommandParseException, CLIException {
        // replace multiple spaces with nothing
        string = string.replaceAll("\\s{2,}", " ");
        CommandStringReader stringReader = new CommandStringReader(string);
        return parse(connection, stringReader, new CommandStack());
    }

    public CommandNode addChildren(Set<CommandNode> children) {
        for (CommandNode child : children) {
            if (child instanceof CommandArgumentNode) {
                this.argumentsChildren.add(((CommandArgumentNode) child));
                continue;
            }
            if (child instanceof CommandLiteralNode) {
                CommandLiteralNode literalNode = (CommandLiteralNode) child;
                this.literalChildren.put(literalNode.getName(), literalNode);
            }
        }
        return this;
    }

    public CommandNode addChildren(CommandNode... children) {
        return addChildren(new HashSet<>(Arrays.asList(children)));
    }

    public void resetChildrenIds() {
        this.childrenIds = null;
    }

    public enum NodeTypes {
        ROOT,
        LITERAL,
        ARGUMENT;

        private static final NodeTypes[] NODE_TYPES = values();

        public static NodeTypes byId(int id) {
            return NODE_TYPES[id];
        }
    }
}
