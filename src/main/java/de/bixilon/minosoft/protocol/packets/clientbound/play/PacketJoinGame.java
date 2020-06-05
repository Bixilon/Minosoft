package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.Difficulty;
import de.bixilon.minosoft.game.datatypes.Dimension;
import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.game.datatypes.LevelType;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

public class PacketJoinGame implements ClientboundPacket {
    int entityId;
    boolean hardcore;
    GameMode gameMode;
    Dimension dimension;
    Difficulty difficulty;
    int maxPlayers;
    LevelType levelType;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                this.entityId = buffer.readInteger();
                byte gameModeRaw = buffer.readByte();
                hardcore = BitByte.isBitSet(gameModeRaw, 3);
                // remove hardcore bit and get gamemode
                gameModeRaw &= ~0x8;
                gameMode = GameMode.byId(gameModeRaw);

                dimension = Dimension.byId(buffer.readByte());
                difficulty = Difficulty.byId(buffer.readByte());
                maxPlayers = buffer.readByte();
                levelType = LevelType.byType(buffer.readString());
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving join game packet (entityId=%s, gameMode=%s, dimension=%s, difficulty=%s, hardcore=%s)", entityId, gameMode.name(), dimension.name(), difficulty.name(), hardcore));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public int getEntityId() {
        return entityId;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Dimension getDimension() {
        return dimension;
    }
}
