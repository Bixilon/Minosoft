package de.bixilon.minosoft.gui.eros

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosWarningDialog
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.OSUtil
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
        if (OSUtil.OS != OSUtil.OSs.MAC || RunConfiguration.DISABLE_RENDERING) {
            return
        }
        if (Minosoft.config.config.general.ignoreXStartOnFirstThreadWarning) {
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
