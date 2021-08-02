/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.main

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.ShutdownReasons
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.modding.invoker.JavaFXEventInvoker
import de.bixilon.minosoft.gui.eros.util.JavaFXAccountUtil.avatar
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.clickable
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.modding.event.events.account.AccountSelectEvent
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.ShutdownManager
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.stage.WindowEvent
import org.kordamp.ikonli.javafx.FontIcon


class MainErosController : JavaFXWindowController() {
    @FXML private lateinit var logoFX: ImageView
    @FXML private lateinit var versionTextFX: Label

    @FXML private lateinit var playIconFX: FontIcon
    @FXML private lateinit var settingsIconFX: FontIcon
    @FXML private lateinit var helpIconFX: FontIcon
    @FXML private lateinit var aboutIconFX: FontIcon
    @FXML private lateinit var exitIconFX: FontIcon

    @FXML private lateinit var contentFX: Pane

    @FXML private lateinit var accountImageFX: ImageView

    @FXML private lateinit var accountNameFX: Label

    private lateinit var iconMap: Map<ErosMainActivities, FontIcon>


    private var activity: ErosMainActivities = ErosMainActivities.ABOUT // other value (just not the default)
        set(value) {
            field = value
            contentFX.children.setAll(JavaFXUtil.loadEmbeddedController<EmbeddedJavaFXController<*>>(field.layout).root)

            highlightIcon(iconMap[value])
        }

    private fun highlightIcon(iconToSelect: FontIcon?) {
        for (icon in iconMap.values) {
            if (icon === iconToSelect) {
                continue
            }
            // ToDo: Set selected
            icon.isDisable = false
        }
        iconToSelect?.apply { isDisable = true }
    }

    override fun init() {
        logoFX.image = JavaFXUtil.MINOSOFT_LOGO
        versionTextFX.text = "Minosoft " + GitInfo.IS_INITIALIZED.decide(GitInfo.GIT_COMMIT_ID_ABBREV, StaticConfiguration.VERSION)
        iconMap = mapOf(
            ErosMainActivities.PlAY to playIconFX,
            ErosMainActivities.SETTINGS to settingsIconFX,
            ErosMainActivities.HELP to helpIconFX,
            ErosMainActivities.ABOUT to aboutIconFX,
        )

        for (icon in iconMap) {
            icon.value.clickable()
        }

        highlightIcon(playIconFX)

        playIconFX.setOnMouseClicked {
            activity = ErosMainActivities.PlAY
        }
        settingsIconFX.setOnMouseClicked {
            // ToDo: activity = ErosMainActivities.SETTINGS
        }
        helpIconFX.setOnMouseClicked {
            // ToDo: activity = ErosMainActivities.HELP
            JavaFXUtil.HOST_SERVICES.showDocument("https://gitlab.bixilon.de/bixilon/minosoft/-/issues/")
        }
        aboutIconFX.setOnMouseClicked {
            // ToDo: activity = ErosMainActivities.ABOUT
            JavaFXUtil.HOST_SERVICES.showDocument("https://gitlab.bixilon.de/bixilon/minosoft/")
        }
        exitIconFX.apply {
            clickable()
            setOnMouseClicked {
                stage.close()
                ShutdownManager.shutdown(reason = ShutdownReasons.REQUESTED_BY_USER)
            }
        }

        GlobalEventMaster.registerEvent(JavaFXEventInvoker.of<AccountSelectEvent> {
            accountImageFX.image = it.account?.avatar
            accountNameFX.ctext = it.account?.username ?: NO_ACCOUNT_SELECTED
        })
        accountImageFX.clickable()
        accountNameFX.clickable()

        activity = ErosMainActivities.PlAY
    }

    override fun postInit() {
        stage.scene.window.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST) {
            ShutdownManager.shutdown(reason = ShutdownReasons.REQUESTED_BY_USER)
        }
    }

    fun verifyAccount(account: Account? = Minosoft.config.config.account.selected, onSuccess: (Account) -> Unit) {
        if (account == null) {
            activity = ErosMainActivities.ACCOUNT
            return
        }

        DefaultThreadPool += {
            try {
                account.verify()
            } catch (exception: Throwable) {
                activity = ErosMainActivities.ACCOUNT
                // ToDo: Show account window and do account error handling
            }
            onSuccess(account)
        }
    }


    @FXML
    fun openAccountActivity() {
        activity = ErosMainActivities.ACCOUNT
    }

    companion object {
        private val NO_ACCOUNT_SELECTED = "minosoft:main.account.no_account_selected".asResourceLocation()
    }
}
