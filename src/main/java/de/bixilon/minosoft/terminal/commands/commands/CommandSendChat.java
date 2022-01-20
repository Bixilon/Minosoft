/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal.commands.commands;

import de.bixilon.minosoft.data.commands.CommandArgumentNode;
import de.bixilon.minosoft.data.commands.CommandLiteralNode;
import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.commands.CommandRootNode;
import de.bixilon.minosoft.data.commands.parser.MessageParser;
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException;
import de.bixilon.minosoft.modding.event.events.ChatMessageSendEvent;
import de.bixilon.minosoft.protocol.packets.c2s.play.chat.ChatMessageC2SP;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogMessageType;
import org.apache.commons.lang3.StringUtils;

public class CommandSendChat extends Command {
    public static final char[] ILLEGAL_CHAT_CHARS = {'§'};

    @Override
    public CommandNode build(CommandNode parent) {
        parent.addChildren(
                new CommandLiteralNode("chat",
                        new CommandArgumentNode("message", MessageParser.MESSAGE_PARSER, (connection, stack) -> {
                            String message = stack.getNonLiteralArgument(0);
                            if (message.startsWith("/")) {
                                // command
                                String command = message.substring(1);
                                try {
                                    CommandRootNode rootNode = connection.getCommandRoot();
                                    if (rootNode != null) {
                                        connection.getCommandRoot().parse(connection, command);
                                    }
                                } catch (CommandParseException e) {
                                    printError("Command \"%s\" is invalid, %s: %s", command, e.getClass().getSimpleName(), e.getMessage());
                                    // return;
                                }
                            }

                            if (StringUtils.isBlank(message)) {
                                // throw new IllegalArgumentException(("Chat message is blank!"));
                                return;
                            }
                            for (char illegalChar : ILLEGAL_CHAT_CHARS) {
                                if (message.indexOf(illegalChar) != -1) {
                                    // throw new IllegalArgumentException(String.format("%s is not allowed in chat", illegalChar));
                                    return;
                                }
                            }
                            ChatMessageSendEvent event = new ChatMessageSendEvent(connection, message);
                            if (connection.fireEvent(event)) {
                                return;
                            }
                            Log.log(LogMessageType.CHAT_OUT, message);
                            connection.sendPacket(new ChatMessageC2SP(event.getMessage()));
                        })));
        return parent;
    }
}
