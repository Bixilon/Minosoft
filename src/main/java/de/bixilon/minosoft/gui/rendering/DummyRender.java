package de.bixilon.minosoft.gui.rendering;

import de.bixilon.minosoft.gui.rendering.exceptions.ShaderLoadingException;
import org.lwjgl.Version;

import java.io.IOException;

public class DummyRender {
    public static void main(String[] args) throws IOException, ShaderLoadingException {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        RenderWindow renderWindow = new RenderWindow();
        renderWindow.init();
        renderWindow.startLoop();
        renderWindow.exit();

    }
}
