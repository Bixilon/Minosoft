package de.bixilon.minosoft.gui.rendering;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {
    private final String texturePath;
    private int textureId;

    public Texture(String texturePath) {
        this.texturePath = texturePath;
    }


    public int load() throws IOException {
        this.textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.textureId);
        // set the texture wrapping/filtering options (on the currently bound texture object)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        // load and generate the texture

        PNGDecoder decoder = new PNGDecoder(OpenGLUtil.class.getResourceAsStream(this.texturePath));
        ByteBuffer buffer = BufferUtils.createByteBuffer(decoder.getWidth() * decoder.getHeight() * 4);
        decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
        buffer.flip();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        return this.textureId;
    }

    public int getTextureId() {
        return this.textureId;
    }

    public void use(int textureMode) {
        glActiveTexture(textureMode); // activate the texture unit first before binding texture
        glBindTexture(GL_TEXTURE_2D, this.textureId);
    }
}
