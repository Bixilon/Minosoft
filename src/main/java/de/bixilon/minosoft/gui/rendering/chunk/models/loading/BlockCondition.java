package de.bixilon.minosoft.gui.rendering.chunk.models.loading;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties;
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations;

import java.util.HashSet;
import java.util.Map;

public class BlockCondition {
    public static final BlockCondition TRUE_CONDITION = new BlockCondition() {
        @Override
        public boolean contains(Block block) {
            return true;
        }
    };

    private HashSet<BlockProperties> properties;
    private BlockRotations rotation;

    public BlockCondition(JsonObject json) {
        properties = new HashSet<>();
        rotation = BlockRotations.NONE;
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String value = entry.getValue().getAsString();
            if (BlockProperties.PROPERTIES_MAPPING.containsKey(entry.getKey())) {
                properties.add(BlockProperties.PROPERTIES_MAPPING.get(entry.getKey()).get(value));
                continue;
            }
            rotation = BlockRotations.ROTATION_MAPPING.get(value);
        }
    }

    public BlockCondition() {
    }

    public boolean contains(Block block) {
        if (rotation != BlockRotations.NONE && rotation != block.getRotation()) {
            return false;
        }
        return block.getProperties().containsAll(properties);
    }
}
