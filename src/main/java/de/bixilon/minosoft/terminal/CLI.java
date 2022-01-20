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

package de.bixilon.minosoft.terminal;

import com.google.common.reflect.ClassPath;
import de.bixilon.kutil.latch.CountUpAndDownLatch;
import de.bixilon.minosoft.ShutdownReasons;
import de.bixilon.minosoft.data.commands.CommandRootNode;
import de.bixilon.minosoft.data.commands.CommandStringReader;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.data.commands.parser.exceptions.UnknownCommandParseException;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.terminal.commands.CommandStack;
import de.bixilon.minosoft.terminal.commands.commands.Command;
import de.bixilon.minosoft.terminal.commands.exceptions.CLIException;
import de.bixilon.minosoft.util.ShutdownManager;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogLevels;
import de.bixilon.minosoft.util.logging.LogMessageType;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Deprecated
public class CLI {
    private static final CommandRootNode ROOT_NODE;
    private static PlayConnection currentConnection;

    static {
        ROOT_NODE = new CommandRootNode();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            for (ClassPath.ClassInfo info : ClassPath.from(classLoader).getTopLevelClasses(Command.class.getPackage().getName())) {
                Class<?> clazz = info.load();
                if (clazz == Command.class) {
                    continue;
                }
                if (!Command.class.isAssignableFrom(clazz)) {
                    continue;
                }
                ((Command) clazz.getConstructor().newInstance()).build(ROOT_NODE);
            }

        } catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static PlayConnection getCurrentConnection() {
        return currentConnection;
    }

    public static void setCurrentConnection(@Nullable PlayConnection connection) {
        currentConnection = connection;
    }

    public static void initialize() throws InterruptedException {
        CountUpAndDownLatch latch = new CountUpAndDownLatch(1);
        new Thread(() -> {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE, () -> "Initializing CLI...");
            try {
                TerminalBuilder builder = TerminalBuilder.builder();

                Terminal terminal = builder.build();
                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        //    .completer() // ToDo
                        .parser(new DefaultParser())
                        .build();


                latch.dec();

                while (true) {
                    try {
                        String line;
                        try {
                            line = reader.readLine().replaceAll("\\s{2,}", "");
                        } catch (UserInterruptException e) {
                            ShutdownManager.INSTANCE.shutdown(e.getMessage(), ShutdownReasons.REQUESTED_BY_USER);
                            return;
                        } catch (Exception other) {
                            other.printStackTrace();
                            Log.log(LogMessageType.GENERAL, LogLevels.INFO, () -> "Failed to read line. Stopping CLI!");
                            return;
                        }
                        terminal.flush();
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        ROOT_NODE.execute(currentConnection, new CommandStringReader(line), new CommandStack());


                    } catch (CLIException | CommandParseException exception) {
                        Command.printError("--> " + exception.getMessage());
                        if (exception instanceof UnknownCommandParseException) {
                            Command.printError("Type help for a command list!");
                        }
                    } catch (UserInterruptException exception) {
                        ShutdownManager.INSTANCE.shutdown(exception.getMessage(), ShutdownReasons.REQUESTED_BY_USER);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "CLI").start();
        latch.await();
    }
}
