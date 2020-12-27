package de.bixilon.minosoft.gui.rendering;

import org.lwjgl.Version;

public class DummyRender {
    public static void main(String[] args) {

        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        RenderWindow renderWindow = new RenderWindow();
        renderWindow.init();
        renderWindow.startLoop();
        renderWindow.exit();

    }
}
