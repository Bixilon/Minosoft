package de.bixilon.minosoft.util;

public final class OSUtil {
    private static OS os;

    public static OS getOS() {
        if (os == null) {
            String name = System.getProperty("os.name");
            if (name.startsWith("Windows")) {
                os = OS.WINDOWS;
            } else if (name.startsWith("Linux")) {
                os = OS.LINUX;
            } else if (name.startsWith("Mac")) {
                os = OS.MAC;
            } else {
                os = OS.OTHER;
            }
        }
        return os;
    }

    public enum OS {
        WINDOWS,
        LINUX,
        MAC,
        OTHER
    }

}
