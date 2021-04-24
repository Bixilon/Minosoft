/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.main;

import com.jfoenix.controls.JFXComboBox;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.util.logging.LogMessageType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class GUITools {
    public static final Image MINOSOFT_LOGO = new Image(GUITools.class.getResourceAsStream("/assets/minosoft/textures/icons/window_icon.png"));
    public static final ObservableList<Version> VERSIONS = FXCollections.observableArrayList();
    public static final JFXComboBox<Version> VERSION_COMBO_BOX = new JFXComboBox<>(VERSIONS);
    public static final ObservableList<LogMessageType> LOG_LEVELS = FXCollections.observableList(Arrays.asList(LogMessageType.values().clone()));

    static {
        VERSIONS.add(Versions.AUTOMATIC_VERSION);
        Versions.getVersionIdMap().forEach((key, value) -> VERSIONS.add(value));

        VERSIONS.sort((a, b) -> {
            if (a.getVersionId() == -1) {
                return -Integer.MAX_VALUE;
            }
            return -(a.getVersionId() - b.getVersionId());
        });
    }

    public static Image getImageFromBase64(String base64) {
        if (base64 == null) {
            return null;
        }
        try {
            return new Image(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Image getImage(byte[] raw) {
        if (raw == null) {
            return null;
        }
        try {
            return new Image(new ByteArrayInputStream(raw));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Scene initializeScene(Scene scene) {
        scene.getStylesheets().add("/assets/minosoft/layout/style.css"); // ToDo: Migrate to minosoft assets manager
        if (scene.getWindow() instanceof Stage stage) {
            stage.getIcons().add(MINOSOFT_LOGO);
        }
        return scene;
    }

    public static Pane initializePane(Pane pane) {
        initializeScene(pane.getScene());
        return pane;
    }

    public static <T> T showPane(ResourceLocation fxmlResourceLocation, Modality modality, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(Minosoft.MINOSOFT_ASSETS_MANAGER.getAssetURL(fxmlResourceLocation));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.initModality(modality);
        double width = 600;
        double height = 400;
        if (root instanceof Pane pane) {
            width = pane.getPrefWidth();
            height = pane.getPrefHeight();
        }
        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);

        stage.setTitle(title);
        initializeScene(scene);

        stage.show();
        return loader.getController();
    }
}
