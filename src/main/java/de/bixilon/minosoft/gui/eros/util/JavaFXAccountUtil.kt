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

import de.bixilon.minosoft.data.accounts.Account
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import java.io.ByteArrayInputStream


object JavaFXAccountUtil {

    val Account.avatar: Image
        get() {
            if (!this.supportsSkins) {
                return JavaFXUtil.MINOSOFT_LOGO
            }
            return this.properties?.textures?.skin?.read()?.let {
                val image = Image(ByteArrayInputStream(it), 0.0, 0.0, true, false)
                return@let WritableImage(image.pixelReader, 8, 8, 8, 8)
            } ?: JavaFXUtil.MINOSOFT_LOGO
        }
}
