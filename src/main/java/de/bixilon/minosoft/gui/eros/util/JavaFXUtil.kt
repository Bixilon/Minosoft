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

package de.bixilon.minosoft.gui.eros.util

import afester.javafx.svg.SvgLoader
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.jvmField
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.controller.JavaFXController
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.text.JavaFXTextRenderer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.crash.freeze.FreezeDumpUtil
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX
import de.bixilon.minosoft.util.system.SystemUtil
import javafx.application.HostServices
import javafx.application.Platform
import javafx.beans.property.BooleanPropertyBase
import javafx.css.StyleableProperty
import javafx.fxml.FXMLLoader
import javafx.scene.*
import javafx.scene.control.Alert
import javafx.scene.control.Labeled
import javafx.scene.control.TableColumnBase
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import java.io.File

object JavaFXUtil {
    private const val DEFAULT_STYLE = "resource:minosoft:eros/style.css"
    private val SHOWING_FIELD = Window::class.java.getDeclaredField("showing").apply { isAccessible = true }
    private val MARK_INVALID_METHOD = BooleanPropertyBase::class.java.getDeclaredMethod("markInvalid").apply { isAccessible = true }
    private val stages = StageList()
    lateinit var JAVA_FX_THREAD: Thread
    lateinit var MINOSOFT_LOGO: Image
    lateinit var HOST_SERVICES: HostServices
    val BIXILON_LOGO: Group? by lazy { catchAll { SvgLoader().loadSvg(IntegratedAssets.DEFAULT[minosoft("textures/icons/bixilon_logo.svg")]) } }
    private var watchingTheme = false

    val THEME_ASSETS_MANAGER = IntegratedAssets.DEFAULT

    private fun startThemeWatcher() {
        if (watchingTheme) {
            return
        }

        ErosProfileManager.selected.theme::theme.observeFX(this) {
            stages.cleanup()
            for (stage in stages.iterator()) {
                stage ?: break
                stage.scene.stylesheets.clear()
                stage.scene.stylesheets.add(DEFAULT_STYLE)
                stage.scene.stylesheets.add(getThemeURL(it))
            }
        }
        watchingTheme = true
    }

    private fun <T : JavaFXController> loadController(title: Any, fxmlLoader: FXMLLoader, parent: Parent, modality: Modality = Modality.WINDOW_MODAL): T {
        val stage = Stage()
        stage.initModality(modality)
        stage.title = IntegratedLanguage.LANGUAGE.translate(title).message
        stage.scene = Scene(parent)
        stage.icons.setAll(MINOSOFT_LOGO)

        stage.scene.stylesheets.add(DEFAULT_STYLE)
        val theme = ErosProfileManager.selected.theme.theme
        stage.scene.stylesheets.add(getThemeURL(theme))

        stages.cleanup()
        stages.add(stage)

        val controller: T = fxmlLoader.getController()

        if (controller is JavaFXWindowController) {
            controller.stage = stage
        }
        controller.postInit()

        return controller
    }

    fun <T : JavaFXController> openModal(title: Any, layout: ResourceLocation, controller: T? = null, modality: Modality = Modality.WINDOW_MODAL): T {
        startThemeWatcher()
        val fxmlLoader = createLoader()
        controller?.let { fxmlLoader.setController(it) }
        val parent: Parent = fxmlLoader.load(IntegratedAssets.DEFAULT[layout])
        parent.registerFreezeDumpKey()
        return loadController(title, fxmlLoader, parent, modality)
    }

    fun <T : JavaFXController> openModalAsync(title: Any, layout: ResourceLocation, controller: T? = null, modality: Modality = Modality.WINDOW_MODAL, callback: ((T) -> Unit)? = null) {
        DefaultThreadPool += add@{
            startThemeWatcher()
            val fxmlLoader = createLoader()
            controller?.let { fxmlLoader.setController(it) }
            val parent: Parent = fxmlLoader.load(IntegratedAssets.DEFAULT[layout])
            parent.registerFreezeDumpKey()

            if (callback == null) {
                return@add
            }

            runLater { callback(loadController(title, fxmlLoader, parent, modality)) }
        }
    }

    fun <T : EmbeddedJavaFXController<out Pane>> loadEmbeddedController(layout: ResourceLocation, controller: T? = null): T {
        val fxmlLoader = createLoader()
        controller?.let { fxmlLoader.setController(it) }
        val pane = fxmlLoader.load<Pane>(IntegratedAssets.DEFAULT[layout])

        val controller = fxmlLoader.getController<T>()

        controller::root.jvmField.forceSet(controller, pane)
        controller.postInit()

        return controller
    }

    var TextFlow.text: Any?
        get() = TODO("Can not get the text of a TextFlow (yet)")
        set(value) {
            this.children.setAll(JavaFXTextRenderer.render(IntegratedLanguage.LANGUAGE.translate(value)))
        }

    var TextField.placeholder: Any?
        get() = this.promptText
        set(value) {
            this.promptText = IntegratedLanguage.LANGUAGE.translate(value).message
        }

    var Labeled.ctext: Any?
        get() = this.text
        set(value) {
            this.text = IntegratedLanguage.LANGUAGE.translate(value).message
        }

    var TableColumnBase<*, *>.ctext: Any?
        get() = this.text
        set(value) {
            this.text = IntegratedLanguage.LANGUAGE.translate(value).message
        }

    var Text.ctext: Any?
        get() = this.text
        set(value) {
            this.text = IntegratedLanguage.LANGUAGE.translate(value).message
        }

    fun Text.hyperlink(link: String) {
        val url = link.toURL()
        this.setOnMouseClicked { DefaultThreadPool += { SystemUtil.api?.openURL(url) } }
        this.accessibleRole = AccessibleRole.HYPERLINK
        this.styleClass.setAll("hyperlink")
        this.clickable()
    }

    fun Text.file(path: File) {
        this.setOnMouseClicked { DefaultThreadPool += { SystemUtil.api?.openFile(path) } }
        this.accessibleRole = AccessibleRole.HYPERLINK
        this.styleClass.setAll("hyperlink")
        this.clickable()
    }

    fun Node.clickable() {
        this.styleClass.add("button")
        this.cursorProperty().unsafeCast<StyleableProperty<Cursor>>().applyStyle(null, Cursor.HAND)
    }

    fun runLater(runnable: Runnable) {
        if (Thread.currentThread() === JAVA_FX_THREAD) {
            runnable.run()
            return
        }

        Platform.runLater(runnable)
    }

    fun Stage.bringToFront() {
        isAlwaysOnTop = true
        this.requestFocus()
        this.toFront()
        isAlwaysOnTop = false
    }

    private fun getThemeURL(name: String): String {
        val path = "minosoft:eros/themes/$name.css"
        if (path.toResourceLocation() !in THEME_ASSETS_MANAGER) {
            throw Exception("Can not load theme: $name")
        }

        return "resource:$path"
    }


    private fun Node.registerFreezeDumpKey() {
        addEventFilter(KeyEvent.KEY_PRESSED) {
            if (!it.isAltDown || it.code != KeyCode.F6) return@addEventFilter
            FreezeDumpUtil.catchAsync { freeze ->
                runLater {
                    val alert = Alert(Alert.AlertType.WARNING)
                    alert.headerText = "Freeze dump created"
                    alert.contentText = "A freeze dump was created and stored at ${freeze.path}"
                    alert.show()
                }
            }
        }
    }

    fun createLoader(): FXMLLoader {
        val loader = FXMLLoader()

        if (loader.classLoader == null) {
            loader.classLoader = FXMLLoader::class.java.classLoader
        }

        return loader
    }

    fun Window.forceInit() {
        val showing = SHOWING_FIELD.get(this)
        MARK_INVALID_METHOD.invoke(showing)
    }
}
