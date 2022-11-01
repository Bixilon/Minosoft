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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.format
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority

class InfoPane<T>(vararg children: Node) : AnchorPane(*children) {
    private var item: T? = null
    private var propertiesPane: GridPane = GridPane()

    constructor() : this(children = emptyArray())

    fun reset() {
        children.clear()
    }

    fun updateProperties(properties: List<Pair<ResourceLocation, (T) -> Any?>>) {
        propertiesPane.children.clear()
        val item = item ?: return
        var row = 0
        for ((key, property) in properties) {
            val propertyValue = property(item) ?: continue

            propertiesPane.add(Minosoft.LANGUAGE_MANAGER.translate(key).textFlow, 0, row)
            propertiesPane.add(propertyValue.format().textFlow, 1, row++)
        }
    }

    fun update(item: T?, properties: List<Pair<ResourceLocation, (T) -> Any?>>, actions: Array<Node>) {
        this.item = item
        if (item == null) {
            reset()
            return
        }

        val pane = GridPane()

        setLeftAnchor(pane, 10.0)
        setRightAnchor(pane, 10.0)


        GridPane().let {
            this.propertiesPane = it
            updateProperties(properties)

            it.columnConstraints += ColumnConstraints(10.0, 180.0, 250.0)
            it.columnConstraints += ColumnConstraints(10.0, 200.0, 300.0)
            it.hgap = 10.0
            it.vgap = 5.0

            pane.add(it, 0, 0)
        }

        GridPane().let {
            for (index in 0 until actions.size / 2) {
                it.columnConstraints += ColumnConstraints()
            }
            it.columnConstraints += ColumnConstraints(0.0, -1.0, Double.POSITIVE_INFINITY, Priority.ALWAYS, HPos.LEFT, true)

            for ((index, action) in actions.withIndex()) {
                it.add(action, index, 0)
            }


            it.hgap = 5.0
            GridPane.setMargin(it, Insets(20.0, 0.0, 0.0, 0.0))

            pane.add(it, 0, 1)
        }


        children.setAll(pane)
    }
}
