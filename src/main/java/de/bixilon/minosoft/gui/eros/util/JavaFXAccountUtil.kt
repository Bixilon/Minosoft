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

package de.bixilon.minosoft.gui.eros.util

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.accounts.Account
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import java.io.ByteArrayInputStream


object JavaFXAccountUtil {
    const val HEAD_SIZE = 8

    val Account.avatar: Image
        get() {
            if (!this.supportsSkins) {
                return JavaFXUtil.MINOSOFT_LOGO
            }
            return this.properties?.textures?.skin?.read()?.let {
                val image = Image(ByteArrayInputStream(it), 0.0, 0.0, true, false)
                val written = WritableImage(image.pixelReader, HEAD_SIZE, HEAD_SIZE, 8, 8)
                if (ErosProfileManager.selected.general.renderSkinOverlay) {
                    written.pixelWriter.writeNonTransparent(0, 0, HEAD_SIZE, HEAD_SIZE, image.pixelReader, 40, 8)
                }
                return@let written
            } ?: JavaFXUtil.MINOSOFT_LOGO
        }

    private fun PixelWriter.writeNonTransparent(dX: Int, dY: Int, width: Int, height: Int, reader: PixelReader, sX: Int, sY: Int) {
        for (x in 0 until width) {
            for (y in 0 until height) {
                val color = reader.getArgb(sX + x, sY + y)
                if (color ushr 24 == 0) {
                    continue
                }
                setArgb(dX + x, dY + y, color)
            }
        }
    }
}
