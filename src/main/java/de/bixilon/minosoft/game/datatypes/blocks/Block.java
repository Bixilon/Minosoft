package de.bixilon.minosoft.game.datatypes.blocks;

import de.bixilon.minosoft.game.datatypes.WorldBlock;

public interface Block extends WorldBlock {

    String getLegacyIdentifier();

    int getLegacyId();

}