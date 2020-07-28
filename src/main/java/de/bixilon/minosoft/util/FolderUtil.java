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

package de.bixilon.minosoft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

public class FolderUtil {

    public static void copyFolder(URI from, String to) throws IOException {
        File folder = new File(from);
        for (String entry : folder.list()) {
            File entryFile = new File(folder.getPath() + File.separator + entry);
            if (entryFile.isDirectory()) {
                copyFolder(entryFile.toURI(), to + File.separator + entry);
                continue;
            }
            File out = new File(to + File.separator + entry);
            if (out.exists()) {
                continue;
            }
            File outFolder = new File(out.getParent());
            if (!outFolder.exists()) {
                outFolder.mkdirs();
            }
            Files.copy(new FileInputStream(entryFile), out.toPath());
        }
    }
}
