/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.main;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsWindow implements Initializable {
    @FXML
    public GridPane tabGeneral;
    @FXML
    public ComboBox<LogLevels> generalLogLevel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        generalLogLevel.setItems(GUITools.logLevels);
        generalLogLevel.getSelectionModel().select(Log.getLevel());
        generalLogLevel.setOnAction((actionEvent -> {
            LogLevels newLevel = generalLogLevel.getValue();
            if (Log.getLevel() == newLevel) {
                return;
            }
            Log.setLevel(newLevel);
            Minosoft.getConfig().putString(ConfigurationPaths.GENERAL_LOG_LEVEL, newLevel.name());
            Minosoft.getConfig().saveToFile();
        }));
    }
}
