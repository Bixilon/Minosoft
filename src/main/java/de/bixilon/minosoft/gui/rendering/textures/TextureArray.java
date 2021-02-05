package de.bixilon.minosoft.gui.rendering.textures;

import de.bixilon.minosoft.data.assets.AssetsManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureArray {
    private final AssetsManager assetsManager;
    private final String[] texturePaths;
    private String[] textureIndexArray;
    private int textureId;

    public TextureArray(AssetsManager assetsManager, String[] texturePaths) {
        this.assetsManager = assetsManager;
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
        var textures = TextureLoader.INSTANCE.loadTextureArray(this.assetsManager, this.texturePaths);

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, 16, 16, textures.size(), 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        this.textureIndexArray = new String[textures.size()];

        int i = 0;
        for (Map.Entry<String, ByteBuffer> entry : textures.entrySet()) {
            this.textureIndexArray[i] = entry.getKey();
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i++, 16, 16, 1, GL_RGBA, GL_UNSIGNED_BYTE, entry.getValue());
        }

        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
        return this.textureId;
    }

    public int getTextureId() {
        return this.textureId;
    }

    public void use(int textureMode) {
        glActiveTexture(textureMode); // activate the texture unit first before binding texture
        glBindTexture(GL_TEXTURE_2D, this.textureId);
    }

    public String[] getTextureIndexArray() {
        return this.textureIndexArray;
    }
}
