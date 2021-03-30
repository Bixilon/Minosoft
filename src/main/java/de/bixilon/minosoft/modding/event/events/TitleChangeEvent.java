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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.title.PacketTitle;

public class TitleChangeEvent extends CancelableEvent {
    private final PacketTitle.TitleActions action;

    // fields depend on action
    private final ChatComponent text;
    private final ChatComponent subText;
    private final int fadeInTime;
    private final int stayTime;
    private final int fadeOutTime;

    public TitleChangeEvent(Connection connection, PacketTitle.TitleActions action, ChatComponent text, ChatComponent subText, int fadeInTime, int stayTime, int fadeOutTime) {
        super(connection);
        this.action = action;
        this.text = text;
        this.subText = subText;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
    }

    public TitleChangeEvent(Connection connection, PacketTitle pkg) {
        super(connection);
        this.action = pkg.getAction();
        this.text = pkg.getText();
        this.subText = pkg.getSubText();
        this.fadeInTime = pkg.getFadeInTime();
        this.stayTime = pkg.getStayTime();
        this.fadeOutTime = pkg.getFadeOutTime();
    }

    public PacketTitle.TitleActions getAction() {
        return this.action;
    }

    public ChatComponent getText() {
        return this.text;
    }

    public ChatComponent getSubText() {
        return this.subText;
    }

    public int getFadeInTime() {
        return this.fadeInTime;
    }

    public int getStayTime() {
        return this.stayTime;
    }

    public int getFadeOutTime() {
        return this.fadeOutTime;
    }
}
