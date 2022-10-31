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

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileSelectEvent
import de.bixilon.minosoft.gui.eros.main.MainErosController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.modding.event.events.FinishBootEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.stage.Window

object Eros {
    private val TITLE = "minosoft:eros_window_title".toResourceLocation()
    private val LAYOUT = "minosoft:eros/main/main.fxml".toResourceLocation()

    lateinit var mainErosController: MainErosController

    var skipErosStartup = false

    var initialized = false
        private set

    var visible: Boolean = false
        private set


    @Synchronized
    fun setVisibility(visible: Boolean) {
        if (visible == this.visible) {
            return
        }
        if (!initialized) {
            return
        }
        if (visible) {
            mainErosController.stage.show()
        } else {
            for (window in Window.getWindows().toSynchronizedSet()) {
                JavaFXUtil.runLater { window.hide() }
            }
        }
        this.visible = visible
    }


    init {
        GlobalEventMaster.register(CallbackEventListener.of<FinishBootEvent> {
            if (skipErosStartup) {
                return@of
            }
            start()
        })

        GlobalEventMaster.register(CallbackEventListener.of<ErosProfileSelectEvent> {
            if (skipErosStartup || !this::mainErosController.isInitialized) {
                return@of
            }
            JavaFXUtil.runLater {
                this.mainErosController.stage.close()
                start()
            }
        })
    }

    fun start() {
        JavaFXUtil.openModalAsync<MainErosController>(TITLE, LAYOUT) {
            mainErosController = it
            it.stage.show()
            initialized = true
            visible = true
        }
    }
}
