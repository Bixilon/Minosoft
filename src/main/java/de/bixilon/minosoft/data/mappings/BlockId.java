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

package de.bixilon.minosoft.data.mappings;

import de.bixilon.minosoft.data.mappings.blocks.Block;

import java.util.HashSet;

public class BlockId extends ModIdentifier {
    private final HashSet<Block> blocks;

    public BlockId(String mod, String identifier, HashSet<Block> blocks) {
        super(mod, identifier);
        this.blocks = blocks;
    }

    public BlockId(String mod, String identifier) {
        super(mod, identifier);
        this.blocks = new HashSet<>();
    }

    public BlockId(String fullIdentifier) {
        super(fullIdentifier);
        this.blocks = new HashSet<>();
    }

    public BlockId(ModIdentifier identifier) {
        super(identifier);
        this.blocks = new HashSet<>();
    }

    public HashSet<Block> getBlocks() {
        return this.blocks;
    }
}
