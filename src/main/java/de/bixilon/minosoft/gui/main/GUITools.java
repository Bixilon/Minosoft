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

package de.bixilon.minosoft.gui.main;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class GUITools {
    public final static Image logo = new Image(GUITools.class.getResourceAsStream("/icons/windowIcon.png"));

    public static Image getImageFromBase64(String base64) {
        if (base64 == null) {
            return null;
        }
        return new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
    }
}
