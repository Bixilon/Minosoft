/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.system

import de.bixilon.minosoft.assets.IntegratedAssets
import java.awt.Taskbar
import java.awt.Toolkit
import java.io.File
import java.net.URL

open class DesktopAPI : SystemAPI {

    init {
        setTaskbarIcon()
    }

    override fun openFile(file: File) = Unit
    override fun openURL(url: URL) = Unit

    private fun Taskbar.setDockIcon() {
        iconImage = Toolkit.getDefaultToolkit().createImage(IntegratedAssets.DEFAULT[SystemUtil.ICON].readAllBytes())
    }

    private fun Taskbar.initialize() {
        if (isSupported(Taskbar.Feature.ICON_IMAGE)) {
            setDockIcon()
        }
    }

    private fun setTaskbarIcon() {
        System.setProperty("java.awt.headless", false.toString())
        if (Taskbar.isTaskbarSupported()) {
            Taskbar.getTaskbar().initialize()
        }
    }
}
