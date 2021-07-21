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

package de.bixilon.minosoft.gui.eros

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.eros.main.MainErosController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.FinishInitializingEvent
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.application.Platform

object Eros {
    private val TITLE = "minosoft:eros_window_title".asResourceLocation()
    private val LAYOUT = "minosoft:eros/main/main.fxml".asResourceLocation()


    init {
        Minosoft.GLOBAL_EVENT_MASTER.registerEvent(CallbackEventInvoker.of<FinishInitializingEvent> {
            Platform.runLater {
                val mainErosController = JavaFXUtil.openModal<MainErosController>(TITLE, LAYOUT)
                mainErosController.stage.show()
            }
        })
    }
}
