package de.bixilon.minosoft.gui.rendering;

import de.bixilon.minosoft.gui.rendering.shader.Shader;
import glm_.vec3.Vec3;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.*;

public class WorldMesh {
    int vAO;
    int vBO;
    Vec3 worldPosition;
    int trianglesCount;

    public WorldMesh(float[] data, Vec3 worldPosition) {
        this.worldPosition = worldPosition;
        this.trianglesCount = data.length / 6; // <- bytes per vertex
        this.vAO = glGenVertexArrays();
        this.vBO = glGenBuffers();

        // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(this.vAO);
        glBindBuffer(GL_ARRAY_BUFFER, this.vBO);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * Float.BYTES, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound vertex buffer object so afterwards we can safely unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void draw(Shader chunkShader) {
        chunkShader.setVec3("worldPosition", this.worldPosition);
        glBindVertexArray(this.vAO);
        glDrawArrays(GL_TRIANGLES, 0, this.trianglesCount);
    }

    public void unload() {
        glDeleteVertexArrays(this.vAO);
        glDeleteBuffers(this.vBO);
    }
}
