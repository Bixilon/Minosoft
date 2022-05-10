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

package de.bixilon.minosoft.gui.eros

import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosWarningDialog
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.stage.Modality

object XStartOnFirstThreadWarning {

    private fun showJavaFXRunningWarning() {
        val dialog = SimpleErosWarningDialog(
            title = "minosoft:x_start_on_first_thread_warning.eros_running.title".toResourceLocation(),
            header = "minosoft:x_start_on_first_thread_warning.eros_running.header".toResourceLocation(),
            description = "minosoft:x_start_on_first_thread_warning.eros_running.description".toResourceLocation(),
            onIgnore = { Eros.start() },
            modality = Modality.APPLICATION_MODAL,
        )
        dialog.show()
        Eros.skipErosStartup = true
    }

    @Synchronized
    fun show() {
        if (PlatformInfo.OS != OSTypes.MAC || RunConfiguration.DISABLE_RENDERING) {
            return
        }
        if (OtherProfileManager.selected.ignoreXStartOnFirstThreadWarning) {
            return
        }
        if (RunConfiguration.X_START_ON_FIRST_THREAD_SET) {
            return showJavaFXRunningWarning()
        }

        val dialog = SimpleErosWarningDialog(
            title = "minosoft:x_start_on_first_thread_warning.title".toResourceLocation(),
            header = "minosoft:x_start_on_first_thread_warning.header".toResourceLocation(),
            description = "minosoft:x_start_on_first_thread_warning.description".toResourceLocation(),
            onIgnore = { Eros.start() },
            modality = Modality.APPLICATION_MODAL,
        )
        dialog.show()
        Eros.skipErosStartup = true
    }
}
