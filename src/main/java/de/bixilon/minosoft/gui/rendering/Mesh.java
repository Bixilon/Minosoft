package de.bixilon.minosoft.gui.rendering;

import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import glm_.vec4.Vec4;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {
    public static final float[][] VERTICIES = {
            {
                    -0.5f, -0.5f, -0.5f, 0.0f,
                    0.5f, -0.5f, -0.5f, 1.0f,
                    0.5f, 0.5f, -0.5f, 2.0f,
                    0.5f, 0.5f, -0.5f, 2.0f,
                    -0.5f, 0.5f, -0.5f, 3.0f,
                    -0.5f, -0.5f, -0.5f, 0.0f,
            },
            {
                    -0.5f, -0.5f, 0.5f, 0.0f,
                    0.5f, -0.5f, 0.5f, 1.0f,
                    0.5f, 0.5f, 0.5f, 2.0f,
                    0.5f, 0.5f, 0.5f, 2.0f,
                    -0.5f, 0.5f, 0.5f, 3.0f,
                    -0.5f, -0.5f, 0.5f, 0.0f,
            },
            {
                    -0.5f, 0.5f, 0.5f, 1.0f,
                    -0.5f, 0.5f, -0.5f, 2.0f,
                    -0.5f, -0.5f, -0.5f, 3.0f,
                    -0.5f, -0.5f, -0.5f, 3.0f,
                    -0.5f, -0.5f, 0.5f, 0.0f,
                    -0.5f, 0.5f, 0.5f, 1.0f,
            },
            {
                    0.5f, 0.5f, 0.5f, 1.0f,
                    0.5f, 0.5f, -0.5f, 2.0f,
                    0.5f, -0.5f, -0.5f, 3.0f,
                    0.5f, -0.5f, -0.5f, 3.0f,
                    0.5f, -0.5f, 0.5f, 0.0f,
                    0.5f, 0.5f, 0.5f, 1.0f,
            },
            {
                    -0.5f, -0.5f, -0.5f, 3.0f,
                    0.5f, -0.5f, -0.5f, 2.0f,
                    0.5f, -0.5f, 0.5f, 1.0f,
                    0.5f, -0.5f, 0.5f, 1.0f,
                    -0.5f, -0.5f, 0.5f, 0.0f,
                    -0.5f, -0.5f, -0.5f, 3.0f,
            },
            {
                    -0.5f, 0.5f, -0.5f, 3.0f,
                    0.5f, 0.5f, -0.5f, 2.0f,
                    0.5f, 0.5f, 0.5f, 1.0f,
                    0.5f, 0.5f, 0.5f, 1.0f,
                    -0.5f, 0.5f, 0.5f, 0.0f,
                    -0.5f, 0.5f, -0.5f, 3.0f,
            }
    };
    int textureLayer;
    int vAO;
    int vBO;

    public Mesh(int textureLayer, Vec3 position) {
        this.textureLayer = textureLayer;
        float[] result = new float[VERTICIES.length * VERTICIES[0].length + (VERTICIES.length * VERTICIES[0].length / 2)];
        int resultIndex = 0;

        var model = new Mat4().translate(position);

        for (float[] side : VERTICIES) {
            for (int vertex = 0; vertex < side.length; ) {
                Vec4 input = new Vec4(side[vertex++], side[vertex++], side[vertex++], 1.0f);
                var output = model.times(input);
                // Log.debug("input=%s; position=%s; output=%s;", input, position, output);
                result[resultIndex++] = output.x;
                result[resultIndex++] = output.y;
                result[resultIndex++] = output.z;
                result[resultIndex++] = side[vertex++];
                result[resultIndex++] = textureLayer;
            }
        }

        this.vAO = glGenVertexArrays();
        this.vBO = glGenBuffers();

        // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(this.vAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, result, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 5 * Float.BYTES, 4 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);

        // note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound vertex buffer object so afterwards we can safely unbind
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void draw() {
        glBindVertexArray(this.vAO);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }
}
