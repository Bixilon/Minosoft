package de.bixilon.minosoft.game.datatypes.blocks;

// Only for reference, will be removed very soon
@Deprecated
public interface BlockRotation {
    enum Barely implements BlockRotation {
        EAST(0),
        NORTH(1),
        SOUTH(2),
        WEST(3);

        Barely(int id) {
        }

    }

    enum Normal implements BlockRotation {
        DOWN(0),
        EAST(1),
        NORTH(2),
        SOUTH(3),
        UP(4),
        WEST(5);

        Normal(int id) {
        }
    }

    enum Extended implements BlockRotation {
        SOUTH(0),
        SOUTH_SOUTH_WEST(1),
        SOUTH_WEST(2),
        WEST_SOUTH_WEST(3),
        WEST(4),
        WEST_NORTH_WEST(5),
        NORTH_WEST(6),
        NORTH_NORTH_WEST(7),
        NORTH(8),
        NORTH_NORTH_EAST(9),
        NORTH_EAST(10),
        EAST_NORTH_EAST(11),
        EAST(12),
        EAST_SOUTH_EAST(13),
        SOUTH_EAST(14),
        SOUTH_SOUTH_EAST(15);

        Extended(int id) {
        }
    }
}
