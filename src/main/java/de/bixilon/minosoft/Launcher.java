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

package de.bixilon.minosoft;

import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Version;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Versions;
import de.bixilon.minosoft.gui.main.GUITools;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.gui.main.ServerListCell;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.Map;


public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        GUITools.versions.add(Versions.getLowestVersionSupported());
        for (Map.Entry<Integer, Version> version : Versions.getVersionMap().entrySet()) {
            GUITools.versions.add(version.getValue());
        }
        Comparator<Version> comparator = Comparator.comparingInt(Version::getProtocolVersion);
        FXCollections.sort(GUITools.versions, comparator);
        GUITools.versions.sort((a, b) -> {
            if (a.getProtocolVersion() == -1) {
                return -Integer.MAX_VALUE;
            }
            return (b.getProtocolVersion() - a.getProtocolVersion());
        });
        ListView<Server> listView = new ListView<>();
        listView.setCellFactory((lv) -> ServerListCell.newInstance());

        ObservableList<Server> servers = FXCollections.observableArrayList();
        servers.addAll(Minosoft.serverList);
        listView.setItems(servers);

        Scene scene = new Scene(new BorderPane(listView), 550, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Minosoft");
        primaryStage.getIcons().add(GUITools.logo);
        primaryStage.show();
    }
}