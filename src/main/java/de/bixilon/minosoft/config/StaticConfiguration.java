/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config;

import com.google.common.base.StandardSystemProperty;
import de.bixilon.minosoft.util.OSUtil;

import java.io.File;

public class StaticConfiguration {
    public static final boolean DEBUG_MODE = true; // if true, additional checks will be made to validate data, ... Decreases performance
    public static final boolean DEBUG_SLOW_LOADING = false; // if true, many Thread.sleep will be executed and the start will be delayed (by a lot)
    public static String CONFIG_FILENAME = "config.json"; // Filename of minosoft's base configuration (located in AppData/Minosoft/config)
    public static boolean SKIP_MOJANG_AUTHENTICATION; // disables all connections to mojang
    public static boolean COLORED_LOG = true; // the log should be colored with ANSI (does not affect base components)
    public static boolean LOG_RELATIVE_TIME; // prefix all log messages with the relative start time in milliseconds instead of the formatted time
    public static boolean VERBOSE_ENTITY_META_DATA_LOGGING; // if true, the entity meta data is getting serialized
    public static boolean HEADLESS_MODE; // if true, no gui, rendering or whatever will be loaded or shown
    public static String HOME_DIRECTORY;
    public static final String TEMPORARY_FOLDER = System.getProperty("java.io.tmpdir", HOME_DIRECTORY + "/tmp/") + "/";

    static {
        // Sets Config.homeDir to the correct folder per OS
        String homeDir;
        homeDir = System.getProperty(StandardSystemProperty.USER_HOME.key());
        if (!homeDir.endsWith(File.separator)) {
            homeDir += "/";
        }
        homeDir += switch (OSUtil.OS) {
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
        HOME_DIRECTORY = folder.getAbsolutePath() + "/";
    }
}
