package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.BitByte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PlayChunkBulk implements ClientboundPacket {
    short chunkColumnCount;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                // ToDo only implement once, not twice (chunk data and chunk bulk)
                this.chunkColumnCount = buffer.readShort();
                int dataLen = buffer.readInteger();
                boolean containsSkyLight = buffer.readBoolean();
                byte[] data = buffer.readBytes(dataLen);

                // decompressing chunk data
                Inflater inflater = new Inflater();
                inflater.setInput(data, 0, dataLen);
                byte[] result = new byte[4096];
                ByteArrayOutputStream stream = new ByteArrayOutputStream(dataLen);
                try {
                    while (!inflater.finished()) {
                        stream.write(result, 0, inflater.inflate(result));
                    }
                } catch (DataFormatException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                InByteBuffer decompressed = new InByteBuffer(stream.toByteArray());

                // chunk meta data
                for (int i = 0; i < chunkColumnCount; i++) {
                    int x = buffer.readInteger();
                    int y = buffer.readInteger();
                    short primaryBitMask = buffer.readShort();
                    short addBitMask = buffer.readShort();
                    System.out.println(String.format("Meta data: %s %s", x, y));

                    for (int c = 0; c < 16; c++) { // max sections per chunks in chunk column
                        if (BitByte.isBitSet(primaryBitMask, c)) {
                            short[] blockType = BitByte.byteArrayToShortArray(decompressed.readBytes(4096)); // 16 * 16 * 16
                            byte[] metadata = decompressed.readBytes(2048); // 16 * 16 * 16 / 2 (only half bit per block)
                            byte[] light;
                            if (containsSkyLight) {
                                light = decompressed.readBytes(2048);
                            }
                            byte[] addBlockType;
                            if (BitByte.isBitSet(addBitMask, c)) {
                                addBlockType = decompressed.readBytes(2048);
                            }
                            byte[] biome = decompressed.readBytes(256);


                        }

                    }
                }

                break;
        }
        log();
    }

    @Override
    public void log() {
        Log.protocol(String.format("Chunk bulk packet received (chunks: %s)", chunkColumnCount));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
