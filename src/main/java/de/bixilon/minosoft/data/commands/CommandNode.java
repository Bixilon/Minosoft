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

package de.bixilon.minosoft.data.commands;

import com.google.errorprone.annotations.DoNotCall;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.buffers.ImprovedStringReader;

import java.util.HashMap;
import java.util.HashSet;

public abstract class CommandNode {
    protected final boolean isExecutable;
    protected final HashSet<CommandArgumentNode> argumentsChildren = new HashSet<>();
    protected final HashMap<String, CommandLiteralNode> literalChildren = new HashMap<>();
    protected final int[] childrenIds;
    protected final int redirectNodeId;
    protected CommandNode redirectNode;

    public CommandNode(byte flags, InByteBuffer buffer) {
        this.isExecutable = BitByte.isBitMask(flags, 0x04);
        childrenIds = buffer.readVarIntArray();
        if (BitByte.isBitMask(flags, 0x08)) {
            redirectNodeId = buffer.readVarInt();
        } else {
            redirectNodeId = -1;
        }
    }

    public boolean isExecutable() {
        return isExecutable;
    }

    public HashMap<String, CommandLiteralNode> getLiteralChildren() {
        return literalChildren;
    }

    public HashSet<CommandArgumentNode> getArgumentsChildren() {
        return argumentsChildren;
    }

    public CommandNode getRedirectNode() {
        return redirectNode;
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
        return redirectNodeId;
    }

    @DoNotCall
    public int[] getChildrenIds() {
        return childrenIds;
    }

    public boolean isSyntaxCorrect(ImprovedStringReader stringReader) {
        String nextArgument = stringReader.getUntilNextCommandArgument();
        if (nextArgument.length() == 0) {
            return isExecutable;
        }
        if (literalChildren.containsKey(nextArgument)) {
            stringReader.skip(nextArgument.length() + ProtocolDefinition.COMMAND_SEPARATOR.length());
            return literalChildren.get(nextArgument).isSyntaxCorrect(stringReader);
        }
        for (CommandArgumentNode argumentNode : argumentsChildren) {
            int currentPosition = stringReader.getPosition();
            if (argumentNode.isSyntaxCorrect(stringReader)) {
                return true;
            }
            stringReader.setPosition(currentPosition);
        }
        return false;
    }

    public boolean isSyntaxCorrect(String string) {
        // replace multiple spaces with nothing
        string = string.replaceAll("\\s{2,}", " ");
        ImprovedStringReader stringReader = new ImprovedStringReader(string);
        return isSyntaxCorrect(stringReader);
    }

    public enum NodeTypes {
        ROOT,
        LITERAL,
        ARGUMENT
    }
}
