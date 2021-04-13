/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.ShutdownReasons;
import de.bixilon.minosoft.config.StaticConfiguration;
import org.apache.commons.cli.*;

import javax.annotation.Nullable;
import java.util.HashMap;

public class MinosoftCommandLineArguments {
    private static final HashMap<Option, CommandLineArgumentHandler> OPTION_HASH_MAP = new HashMap<>();
    private static final Options OPTIONS = new Options();
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();

    static {
        registerDefaultArguments();
    }

    public static void parseCommandLineArguments(String[] args) {
        OPTION_HASH_MAP.forEach((option, commandLineArgumentHandler) -> OPTIONS.addOption(option));

        try {
            CommandLine commandLine = new DefaultParser().parse(OPTIONS, args);

            for (Option option : commandLine.getOptions()) {
                if (!OPTION_HASH_MAP.containsKey(option)) {
                    continue;
                }
                OPTION_HASH_MAP.get(option).handle(option.getValue());
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HELP_FORMATTER.printHelp("java -jar Minosoft.jar", OPTIONS);
            Minosoft.shutdown(e.getMessage(), ShutdownReasons.CLI_WRONG_PARAMETER);
        }
    }

    public static void registerCommandLineOption(Option option, CommandLineArgumentHandler handler) {
        OPTION_HASH_MAP.put(option, handler);
    }

    private static void registerDefaultArguments() {
        registerCommandLineOption(new Option("?", "help", false, "Displays this help"), (value -> {
            HELP_FORMATTER.printHelp("java -jar Minosoft.jar", OPTIONS);
            Minosoft.shutdown(ShutdownReasons.CLI_HELP);
        }));
        registerCommandLineOption(new Option("home_folder", true, "Home of Minosoft"), (value -> StaticConfiguration.HOME_DIRECTORY = value + "/"));
        registerCommandLineOption(new Option("colored_log", true, "Should the log be colored"), (value -> StaticConfiguration.COLORED_LOG = Boolean.parseBoolean(value)));
        registerCommandLineOption(new Option("verbose_entity_logging", true, "Should entity meta data be printed"), (value -> StaticConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING = Boolean.parseBoolean(value)));
        registerCommandLineOption(new Option("log_time_relativ", true, "Should time in log timestamp be relative"), (value -> StaticConfiguration.LOG_RELATIVE_TIME = Boolean.parseBoolean(value)));
        registerCommandLineOption(new Option("config_filename", true, "The name of the config file (defaults to config.json)"), (value -> StaticConfiguration.CONFIG_FILENAME = value));
        registerCommandLineOption(new Option("skip_mojang_authentication", true, "Debug: Disable all connections to mojang"), (value -> StaticConfiguration.SKIP_MOJANG_AUTHENTICATION = Boolean.parseBoolean(value)));
        registerCommandLineOption(new Option("headless", true, "Disables all GUI parts"), (value -> StaticConfiguration.HEADLESS_MODE = Boolean.parseBoolean(value)));
    }

    public interface CommandLineArgumentHandler {
        void handle(@Nullable String value);
    }
}
