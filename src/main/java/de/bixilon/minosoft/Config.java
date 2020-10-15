/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft;

import de.bixilon.minosoft.util.OSUtil;

import java.io.File;

public class Config {
    public static final String configFileName = "config.json"; // Path of the minosoft base configuration (located in AppData/Minosoft/config)
    public static final boolean skipAuthentication = false; // disables all connections to mojang
    public static final boolean colorLog = true; // the log should be colored with ANSI (does not affect chat components)
    public static final boolean logRelativeTime = false; // prefix all log messages with the relative start time in milliseconds instead of the formatted time

    public static String homeDir;

    static {
        // Sets Config.homeDir to the correct folder per OS
        homeDir = System.getProperty("user.home");
        if (!homeDir.endsWith(File.separator)) {
            homeDir += "/";
        }
        homeDir += switch (OSUtil.getOS()) {
            case LINUX -> ".local/share/minosoft/";
            case WINDOWS -> "AppData/Roaming/Minosoft/";
            case MAC -> "Library/Application Support/Minosoft/";
            case OTHER -> ".minosoft/";
        };
        File folder = new File(homeDir);
        if (!folder.exists() && !folder.mkdirs()) {
            // failed creating folder
            throw new RuntimeException(String.format("Could not create home folder (%s)!", homeDir));
        }
    }
}
