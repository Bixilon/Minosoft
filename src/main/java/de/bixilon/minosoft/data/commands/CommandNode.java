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
import de.bixilon.minosoft.util.BitByte;

import java.util.HashSet;

public abstract class CommandNode {
    protected final boolean isExecutable;
    protected final HashSet<CommandNode> children = new HashSet<>();
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

    public HashSet<CommandNode> getChildren() {
        return children;
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

    public enum NodeTypes {
        ROOT,
        LITERAL,
        ARGUMENT
    }
}
