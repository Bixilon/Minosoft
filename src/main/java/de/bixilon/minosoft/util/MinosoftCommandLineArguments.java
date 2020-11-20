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

package de.bixilon.minosoft.util;

import de.bixilon.minosoft.config.StaticConfiguration;
import org.apache.commons.cli.*;

public class MinosoftCommandLineArguments {

    public static void parseCommandLineArguments(String[] args) {
        Options options = new Options();

        Option help = new Option("?", "help", false, "Displays this help");
        options.addOption(help);

        Option homeFolder = new Option("home_folder", true, "Home of Minosoft");
        options.addOption(homeFolder);

        Option coloredLog = new Option("colored_log", true, "Should the log be colored");
        options.addOption(coloredLog);

        Option verboseEntityLogLevel = new Option("verbose_entity_logging", true, "Should entity meta data be printed");
        options.addOption(verboseEntityLogLevel);

        Option relativeTimeLogging = new Option("log_time_relativ", true, "Should time in log timestamp be relative");
        options.addOption(relativeTimeLogging);

        HelpFormatter formatter = new HelpFormatter();
        CommandLine commandLine;


        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar Minosoft.jar", options);

            System.exit(1);
            return;
        }
        if (commandLine.hasOption(help.getOpt())) {
            formatter.printHelp("java -jar Minosoft.jar", options);
            System.exit(1);
            return;
        }
        if (commandLine.hasOption(homeFolder.getOpt())) {
            StaticConfiguration.HOME_DIRECTORY = commandLine.getOptionValue(homeFolder.getOpt());
        }
        if (commandLine.hasOption(coloredLog.getOpt())) {
            StaticConfiguration.COLORED_LOG = Boolean.parseBoolean(commandLine.getOptionValue(coloredLog.getOpt()));
        }
        if (commandLine.hasOption(verboseEntityLogLevel.getOpt())) {
            StaticConfiguration.VERBOSE_ENTITY_META_DATA_LOGGING = Boolean.parseBoolean(commandLine.getOptionValue(verboseEntityLogLevel.getOpt()));
        }
        if (commandLine.hasOption(relativeTimeLogging.getOpt())) {
            StaticConfiguration.LOG_RELATIVE_TIME = Boolean.parseBoolean(commandLine.getOptionValue(relativeTimeLogging.getOpt()));
        }


    }
}
