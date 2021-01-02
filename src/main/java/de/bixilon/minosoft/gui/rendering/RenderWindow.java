package de.bixilon.minosoft.gui.rendering;

import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException;
import glm_.glm;
import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;


public class RenderWindow {
    private final float[] vertices = {
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
            0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
            -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
            -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    };
    private final Vec3[] cubePositions = {
            new Vec3(0.0f, 0.0f, 0.0f),
            new Vec3(2.0f, 5.0f, -15.0f),
            new Vec3(-1.5f, -2.2f, -2.5f),
            new Vec3(-3.8f, -2.0f, -12.3f),
            new Vec3(2.4f, -0.4f, -3.5f),
            new Vec3(-1.7f, 3.0f, -7.5f),
            new Vec3(1.3f, -2.0f, -2.5f),
            new Vec3(1.5f, 2.0f, -2.5f),
            new Vec3(1.5f, 0.2f, -1.5f),
            new Vec3(-1.3f, 1.0f, -1.5f)
    };
    private int screenWidth = 800;
    private int screenHeight = 600;
    private float visibilityLevel;
    private boolean polygonEnabled;
    private Shader shader;
    private Texture texture0;
    private Texture texture1;
    private long windowId;

    private double deltaTime;    // time between current frame and last frame
    private double lastFrame;

    private Camera camera;

    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        this.windowId = glfwCreateWindow(this.screenWidth, this.screenHeight, "Hello World!", NULL, NULL);
        if (this.windowId == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }

        this.camera = new Camera(45f, this.windowId);


        glfwSetWindowSizeCallback(this.windowId, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                glViewport(0, 0, width, height);
                RenderWindow.this.screenWidth = width;
                RenderWindow.this.screenHeight = height;
                RenderWindow.this.camera.calculateProjectionMatrix(RenderWindow.this.screenWidth, RenderWindow.this.screenHeight, RenderWindow.this.shader);
            }
        });


        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(this.windowId, (window, key, scancode, action, mods) -> {
            if (action != GLFW_RELEASE) {
                return;
            }
            switch (key) {
                case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(this.windowId, true);
                case GLFW_KEY_P -> switchPolygonMode();
            }
        });

        glfwSetInputMode(this.windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED);


        glfwSetCursorPosCallback(this.windowId, ((window, xPos, yPos) -> this.camera.mouseCallback(xPos, yPos)));


        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(this.windowId, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(this.windowId, (videoMode.width() - pWidth.get(0)) / 2, (videoMode.height() - pHeight.get(0)) / 2);
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(this.windowId);
        // Enable v-sync
        glfwSwapInterval(1);


        // Make the window visible
        glfwShowWindow(this.windowId);

        GL.createCapabilities();
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        glEnable(GL_DEPTH_TEST);
    }

    public void startLoop() throws IOException, ShaderLoadingException {
        int vAO = glGenVertexArrays();
        int vBO = glGenBuffers();

        // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vAO);

        glBindBuffer(GL_ARRAY_BUFFER, vBO);
        glBufferData(GL_ARRAY_BUFFER, this.vertices, GL_STATIC_DRAW);


        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, (3 * Float.BYTES));
        glEnableVertexAttribArray(1);

        // note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound vertex buffer object so afterwards we can safely unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // remember: do NOT unbind the EBO while a VAO is active as the bound element buffer object IS stored in the VAO; keep the EBO bound.
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // You can unbind the VAO afterwards so other VAO calls won't accidentally modify this VAO, but this rarely happens. Modifying other
        // VAOs requires a call to glBindVertexArray anyways so we generally don't unbind VAOs (nor VBOs) when it's not directly necessary.
        glBindVertexArray(0);

        this.texture0 = new Texture("/textures/emerald_block.png");
        this.texture0.load();
        this.texture1 = new Texture("/textures/brown_wool.png");
        this.texture1.load();

        this.shader = new Shader("vertex.glsl", "fragment.glsl");
        this.shader.load();
        this.shader.use();


        this.shader.setInt("texture0", 0);
        this.shader.setInt("texture1", 1);

        this.shader.setFloat("visibility", this.visibilityLevel);

        this.camera.calculateProjectionMatrix(this.screenWidth, this.screenHeight, this.shader);
        this.camera.calculateViewMatrix(this.shader);


        while (!glfwWindowShouldClose(this.windowId)) {


            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            double currentFrame = glfwGetTime();


            this.deltaTime = currentFrame - this.lastFrame;
            this.lastFrame = currentFrame;

            this.camera.calculateViewMatrix(this.shader);


            this.texture0.use(GL_TEXTURE0);
            this.texture1.use(GL_TEXTURE1);

            this.shader.use();


            glBindVertexArray(vAO); // seeing as we only have a single VAO there's no need to bind it every time, but we'll do so to keep things a bit more organized

            for (int i = 0; i < this.cubePositions.length; i++) {
                float angle = 50.0f * (i + 1) * (float) glfwGetTime();
                Mat4 model = new Mat4().translate(this.cubePositions[i]).rotate(glm.INSTANCE.radians(angle), new Vec3(i / 0.5f + 0.1f, i / 0.3f + 0.1f, i / 0.1f + 0.1f));
                this.shader.setMat4("model", model);

                glDrawArrays(GL_TRIANGLES, 0, 36);
            }


            glfwSwapBuffers(this.windowId); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            handleInput();
            this.camera.handleInput(this.deltaTime);
        }
    }


    public void exit() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(this.windowId);
        glfwDestroyWindow(this.windowId);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void switchPolygonMode() {
        glPolygonMode(GL_FRONT_AND_BACK, (this.polygonEnabled ? GL_LINE : GL_FILL));
        this.polygonEnabled = !this.polygonEnabled;
    }

    private void handleInput() {
        if (glfwGetKey(this.windowId, GLFW_KEY_UP) == GLFW_PRESS) {
            this.shader.use();
            this.visibilityLevel += 0.1f;
            if (this.visibilityLevel > 1.0f) {
                this.visibilityLevel = 1.0f;
            }
            this.shader.setFloat("visibility", this.visibilityLevel);
        }
        if (glfwGetKey(this.windowId, GLFW_KEY_DOWN) == GLFW_PRESS) {
            this.shader.use();
            this.visibilityLevel -= 0.1f;
            if (this.visibilityLevel < 0.0f) {
                this.visibilityLevel = 0.0f;
            }
            this.shader.setFloat("visibility", this.visibilityLevel);
        }
    }
}
