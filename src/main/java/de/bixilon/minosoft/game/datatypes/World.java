package de.bixilon.minosoft.game.datatypes;

import java.util.HashMap;

/**
 * Collection of ChunkColumns
 */
public class World {
    public final HashMap<ChunkColumnLocation, ChunkColumn> chunks;
    final String name;

    public World(String name) {
        this.name = name;
        chunks = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public ChunkColumn getChunkColumn(ChunkColumnLocation loc) {
        return chunks.get(loc);
    }
}
