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

package de.bixilon.minosoft.gui.eros.main

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.main.account.AccountController
import de.bixilon.minosoft.gui.eros.util.JavaFXAccountUtil.avatar
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.clickable
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.PixelImageView
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.stage.WindowEvent
import org.kordamp.ikonli.javafx.FontIcon


class MainErosController : JavaFXWindowController() {
    @FXML private lateinit var logoFX: ImageView
    @FXML private lateinit var versionTextFX: Label

    @FXML private lateinit var playIconFX: FontIcon
    @FXML private lateinit var profilesIconFX: FontIcon
    @FXML private lateinit var modsIconFX: FontIcon
    @FXML private lateinit var helpIconFX: FontIcon
    @FXML private lateinit var aboutIconFX: FontIcon
    @FXML private lateinit var exitIconFX: FontIcon

    @FXML private lateinit var contentFX: Pane

    @FXML private lateinit var accountFX: GridPane
    @FXML private lateinit var accountImageFX: PixelImageView
    @FXML private lateinit var accountNameFX: Label

    private lateinit var iconMap: Map<ErosMainActivities, FontIcon>

    private val controllers: MutableMap<ErosMainActivities, EmbeddedJavaFXController<*>> = mutableMapOf()

    private var activity: ErosMainActivities = ErosMainActivities.ABOUT // other value (just not the default)
        set(value) {
            if (field === value) {
                return
            }
            field = value
            contentFX.children.setAll(getController(activity).root)

            highlightIcon(iconMap[value])
        }

    private fun getController(activity: ErosMainActivities): EmbeddedJavaFXController<*> {
        return controllers.getOrPut(activity) { JavaFXUtil.loadEmbeddedController(activity.layout) }
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
        versionTextFX.text = RunConfiguration.APPLICATION_NAME
        iconMap = mapOf(
            ErosMainActivities.PLAY to playIconFX,
            ErosMainActivities.PROFILES to profilesIconFX,
            ErosMainActivities.MODS to modsIconFX,
            ErosMainActivities.HELP to helpIconFX,
            ErosMainActivities.ABOUT to aboutIconFX,
        )

        for (icon in iconMap) {
            icon.value.clickable()
        }

        highlightIcon(playIconFX)

        playIconFX.setOnMouseClicked {
            activity = ErosMainActivities.PLAY
        }
        profilesIconFX.setOnMouseClicked {
            activity = ErosMainActivities.PROFILES
        }
        modsIconFX.setOnMouseClicked {
            activity = ErosMainActivities.MODS
        }
        helpIconFX.setOnMouseClicked {
            // ToDo: activity = ErosMainActivities.HELP
            JavaFXUtil.HOST_SERVICES.showDocument("https://gitlab.bixilon.de/bixilon/minosoft/-/issues/")
        }
        aboutIconFX.setOnMouseClicked {
            activity = ErosMainActivities.ABOUT
        }
        exitIconFX.apply {
            clickable()
            setOnMouseClicked {
                stage.close()
                ShutdownManager.shutdown(reason = AbstractShutdownReason.DEFAULT)
            }
        }

        val profile = ErosProfileManager.selected.general.accountProfile
        profile::selected.observeFX(this, true) {
            if (profile != ErosProfileManager.selected.general.accountProfile) {
                return@observeFX
            }
            if (it == null) {
                accountImageFX.isManaged = false
                accountImageFX.isVisible = false
                accountNameFX.ctext = NO_ACCOUNT_SELECTED
            } else {
                accountImageFX.isManaged = true
                accountImageFX.isVisible = true
                accountImageFX.image = it.avatar
                accountNameFX.ctext = it.username
            }
        }
        accountFX.clickable()

        activity = ErosMainActivities.PLAY
    }

    override fun postInit() {
        stage.scene.window.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST) {
            ShutdownManager.shutdown(reason = AbstractShutdownReason.DEFAULT)
        }
    }

    @Synchronized
    fun verifyAccount(account: Account? = null, onSuccess: (Account) -> Unit) {
        val profile = ErosProfileManager.selected.general.accountProfile
        val account = account ?: profile.selected
        if (account == null) {
            activity = ErosMainActivities.ACCOUNT
            return
        }

        getController(ErosMainActivities.ACCOUNT).unsafeCast<AccountController>().checkAccount(account, false, onSuccess = onSuccess)
    }


    @FXML
    fun openAccountActivity() {
        activity = ErosMainActivities.ACCOUNT
    }

    companion object {
        private val NO_ACCOUNT_SELECTED = "minosoft:main.account.no_account_selected".toResourceLocation()
    }
}
