package de.bixilon.minosoft.gui.rendering;

import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashMap;

public class DummyData {
    private static final Block BEDROCK = new Block("bedrock");
    private static final Block DIRT = new Block("dirt");
    private static final Block STONE = new Block("stone");

    public static Chunk getDummyChunk() {
        Chunk chunk = new Chunk(new HashMap<>());
        for (int y = 0; y < ProtocolDefinition.SECTION_HEIGHT_Y * ProtocolDefinition.SECTIONS_PER_CHUNK; y++) {
            for (int x = 0; x < ProtocolDefinition.SECTION_WIDTH_X; x++) {
                for (int z = 0; z < ProtocolDefinition.SECTION_WIDTH_Z; z++) {
                    if (y == 0) {
                        chunk.setBlock(x, y, z, BEDROCK);
                    } else if (y < 8) {
                        chunk.setBlock(x, y, z, DIRT);
                    } else if (y < 60) {
                        chunk.setBlock(x, y, z, STONE);
                    }
                }
            }
        }
        return chunk;
    }
}
