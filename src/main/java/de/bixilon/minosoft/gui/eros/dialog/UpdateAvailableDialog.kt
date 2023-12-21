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

package de.bixilon.minosoft.gui.eros.dialog

import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.registries.identified.Namespaces.i18n
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.updater.MinosoftUpdate
import de.bixilon.minosoft.updater.MinosoftUpdater
import de.bixilon.minosoft.updater.UpdateProgress
import de.bixilon.minosoft.util.system.SystemUtil
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.TextFlow
import javafx.stage.Modality

class UpdateAvailableDialog(
    val update: MinosoftUpdate,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow

    @FXML private lateinit var releaseNotesFX: TextFlow

    @FXML private lateinit var dismissButtonFX: Button
    @FXML private lateinit var laterButtonFX: Button
    @FXML private lateinit var openButtonFX: Button
    @FXML private lateinit var updateButtonFX: Button

    public override fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, Modality.APPLICATION_MODAL)
            super.show()
        }
    }

    override fun init() {
        headerFX.text = IntegratedLanguage.LANGUAGE.translate(HEADER, data = arrayOf(update.name, update.id))
        releaseNotesFX.text = update.releaseNotes ?: NO_NOTES

        dismissButtonFX.ctext = DISMISS; laterButtonFX.ctext = LATER; openButtonFX.ctext = OPEN; updateButtonFX.ctext = UPDATE

        openButtonFX.isDisable = update.page === null
        updateButtonFX.isDisable = update.download === null
    }

    override fun postInit() {
        stage.scene.root.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ESCAPE) {
                close()
            }
        }
    }

    @FXML
    fun dismiss() {
        OtherProfileManager.selected.updater.dismiss = update.id
        close()
    }

    @FXML
    fun later() {
        close()
    }

    @FXML
    fun open() {
        update.page?.let { SystemUtil.api?.openURL(it) }
    }

    @FXML
    fun update() {
        val progress = UpdateProgress()
        MinosoftUpdater.download(update, progress)
        // TODO: Show progress
    }


    companion object {
        private val LAYOUT = minosoft("eros/dialog/update_available.fxml")

        private val TITLE = i18n("updater.available.title")
        private val HEADER = i18n("updater.available.header")
        private val NO_NOTES = i18n("updater.available.no_notes")

        private val DISMISS = i18n("updater.available.dismiss")
        private val LATER = i18n("updater.available.later")
        private val OPEN = i18n("updater.available.open")
        private val UPDATE = i18n("updater.available.update")
    }
}
