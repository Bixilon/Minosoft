package de.bixilon.minosoft.gui.rendering;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureArray {
    private final String[] texturePaths;
    private int textureId;

    public TextureArray(String[] texturePaths) {
        this.texturePaths = texturePaths;
    }

    public int load() throws IOException {
        this.textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, this.textureId);
        // set the texture wrapping/filtering options (on the currently bound texture object)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        // load and generate the texture

        boolean sizeSet = false;

        for (int i = 0; i < this.texturePaths.length; i++) {
            PNGDecoder decoder = new PNGDecoder(OpenGLUtil.class.getResourceAsStream(this.texturePaths[i]));
            ByteBuffer buffer = BufferUtils.createByteBuffer(decoder.getWidth() * decoder.getHeight() * 4);
            decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buffer.flip();
            if (!sizeSet) {
                glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), this.texturePaths.length, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

                sizeSet = true;
            }
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, decoder.getWidth(), decoder.getHeight(), 1, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        }

        //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
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
