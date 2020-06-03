package de.bixilon.minosoft.game.datatypes.blocks;

public interface BlockRotation {

    int getId();

    enum RotationType {
        BARELY,
        NORMAL,
        EXTENDED
    }

    enum Barely implements BlockRotation {
        EAST(0),
        NORTH(1),
        SOUTH(2),
        WEST(3);

        int id;

        Barely(int id) {
            this.id = id;
        }

        public static Barely byId(int id) {
            for (Barely b : values()) {
                if (b.getId() == id) {
                    return b;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    enum Normal implements BlockRotation {
        DOWN(0),
        EAST(1),
        NORTH(2),
        SOUTH(3),
        UP(4),
        WEST(5);

        int id;

        Normal(int id) {
            this.id = id;
        }

        public static Normal byId(int id) {
            for (Normal n : values()) {
                if (n.getId() == id) {
                    return n;
                }
            }
            return null;
        }

        public int getId() {
            return id;
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

        int id;

        Extended(int id) {
            this.id = id;
        }

        public static Extended byId(int id) {
            for (Extended e : values()) {
                if (e.getId() == id) {
                    return e;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
