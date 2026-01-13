/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.options

import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.language.LanguageUtil.i18n
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.input.slider.SliderElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.GUIBuilder
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

class ControlsSettingsMenu(guiRenderer: GUIRenderer) : Screen(guiRenderer), AbstractLayout<Element> {
    private val controlsProfile = guiRenderer.context.session.profiles.controls

    private val titleElement = TextElement(guiRenderer, "minosoft:key.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
    private val doneButton = ButtonElement(guiRenderer, "menu.options.done".i18n()) { guiRenderer.gui.pop() }.apply { parent = this@ControlsSettingsMenu }
    private val resetButton = ButtonElement(guiRenderer, "minosoft:key.reset_all".i18n()) { resetAllBindings() }.apply { parent = this@ControlsSettingsMenu }

    private val listItems: MutableList<ListItem> = mutableListOf()
    private val keyBindingEntries: MutableList<KeyBindingEntry> = mutableListOf()
    private var scrollOffset = 0
    private val maxVisibleEntries = 8

    private var isDraggingScrollbar = false
    private var dragStartY = 0f
    private var dragStartScrollOffset = 0

    private var editingEntry: KeyBindingEntry? = null
    private val pressedKeys = linkedSetOf<KeyCodes>()
    private var peakPressedKeys = listOf<KeyCodes>()

    override var activeElement: Element? = null
    override var activeDragElement: Element? = null

    // Mouse sensitivity slider (range matches profile limits: 0.01 to 10.0, displayed as 1% to 1000%)
    private val sensitivitySlider = SliderElement(guiRenderer, translate("minosoft:key.mouse_sensitivity"), 1.0f, 200.0f, controlsProfile.mouse.sensitivity * 100f) {
        controlsProfile.mouse.sensitivity = it / 100.0f
    }.apply { parent = this@ControlsSettingsMenu }

    init {
        buildKeyBindingEntries()
        forceSilentApply()
    }

    private fun buildKeyBindingEntries() {
        listItems.clear()
        keyBindingEntries.clear()
        
        // Build entries from the categorized list of default keybindings
        for ((category, bindings) in CATEGORIZED_KEYBINDINGS) {
            val header = CategoryHeader(guiRenderer, category).apply { parent = this@ControlsSettingsMenu }
            listItems += header
            for ((name, defaultBinding) in bindings) {
                val currentBinding = controlsProfile.bindings[name] ?: defaultBinding
                val entry = KeyBindingEntry(guiRenderer, name, currentBinding, defaultBinding, this).apply { parent = this@ControlsSettingsMenu }
                listItems += entry
                keyBindingEntries += entry
            }
        }
    }

    private fun resetAllBindings() {
        for (entry in keyBindingEntries) {
            val existingBinding = controlsProfile.bindings[entry.bindingName]
            if (existingBinding != null) {
                existingBinding.action.clear()
                for ((action, codes) in entry.defaultBinding.action) {
                    existingBinding.action[action] = codes.toMutableSet()
                }
                entry.updateBinding(existingBinding)
            } else {
                entry.updateBinding(entry.defaultBinding)
            }
        }
        controlsProfile.storage?.invalidate()
        guiRenderer.context.input.bindings.clear()
        cacheUpToDate = false
    }

    fun startEditing(entry: KeyBindingEntry) {
        editingEntry?.isEditing = false
        
        editingEntry = entry
        entry.isEditing = true
        pressedKeys.clear()
        peakPressedKeys = emptyList()
        cacheUpToDate = false
    }

    fun stopEditing(keys: List<KeyCodes>) {
        val entry = editingEntry ?: return
        entry.isEditing = false
        
        if (keys.isNotEmpty() && KeyCodes.KEY_ESCAPE !in keys) {
            val defaultAction = entry.defaultBinding.action.keys.firstOrNull() ?: KeyActions.CHANGE
            
            val newAction: Map<KeyActions, Set<KeyCodes>> = if (keys.size == 1) {
                mapOf(defaultAction to keys.toSet())
            } else {
                mapOf(
                    KeyActions.MODIFIER to keys.dropLast(1).toSet(),
                    defaultAction to setOf(keys.last())
                )
            }
            
            val existingBinding = controlsProfile.bindings[entry.bindingName]
            if (existingBinding != null) {
                existingBinding.action.clear()
                for ((action, codes) in newAction) {
                    existingBinding.action[action] = codes.toMutableSet()
                }
                entry.updateBinding(existingBinding)
            } else {
                val newBinding = KeyBinding(newAction)
                controlsProfile.bindings[entry.bindingName] = newBinding
                entry.updateBinding(newBinding)
            }
            
            controlsProfile.storage?.invalidate()
            guiRenderer.context.input.bindings.clear()
        }
        
        editingEntry = null
        pressedKeys.clear()
        peakPressedKeys = emptyList()
        cacheUpToDate = false
    }

    private fun calculateElementWidth(): Float {
        return maxOf(size.x * WIDTH_PERCENTAGE, MIN_BUTTON_WIDTH)
    }

    private fun translate(key: String): String {
        return IntegratedLanguage.LANGUAGE.forceTranslate(key.i18n().translationKey).message
    }

    override fun forceSilentApply() {
        titleElement.silentApply()
        val elementWidth = calculateElementWidth()
        
        titleElement.prefMaxSize = Vec2f(elementWidth, -1f)
        doneButton.size = Vec2f(elementWidth / 2 - 2f, doneButton.size.y)
        resetButton.size = Vec2f(elementWidth / 2 - 2f, resetButton.size.y)
        sensitivitySlider.size = Vec2f(elementWidth, sensitivitySlider.size.y)
        
        for (item in listItems) {
            when (item) {
                is CategoryHeader -> {
                    item.setContainerWidth(elementWidth)
                    item.size = Vec2f(elementWidth, CATEGORY_HEIGHT)
                }
                is KeyBindingEntry -> item.size = Vec2f(elementWidth, ENTRY_HEIGHT)
            }
        }
        
        super.forceSilentApply()
        cacheUpToDate = false
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val screenSize = size
        val elementWidth = calculateElementWidth()
        val currentOffset = MVec2f(offset)

        val visibleCount = minOf(maxVisibleEntries, listItems.size)
        val listHeight = visibleCount * (ENTRY_HEIGHT + ENTRY_Y_MARGIN) - ENTRY_Y_MARGIN
        val totalHeight = titleElement.size.y + SPACING + 
                         sensitivitySlider.size.y + SPACING +
                         listHeight + ENTRY_Y_MARGIN +
                         SPACING + BUTTON_HEIGHT
        
        currentOffset.y += (screenSize.y - totalHeight) / 2
        currentOffset.x += (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2

        titleElement.render(currentOffset.unsafe + Vec2f((elementWidth - titleElement.size.x) / 2, 0f), consumer, options)
        currentOffset.y += titleElement.size.y + SPACING

        sensitivitySlider.render(currentOffset.unsafe + Vec2f((elementWidth - sensitivitySlider.size.x) / 2, 0f), consumer, options)
        currentOffset.y += sensitivitySlider.size.y + SPACING

        val listStartY = currentOffset.y
        val startIndex = scrollOffset
        val endIndex = minOf(startIndex + maxVisibleEntries, listItems.size)
        
        for (i in startIndex until endIndex) {
            val item = listItems[i]
            val itemHeight = when (item) {
                is CategoryHeader -> CATEGORY_HEIGHT
                is KeyBindingEntry -> ENTRY_HEIGHT
            }
            item.render(currentOffset.unsafe + Vec2f((elementWidth - item.size.x) / 2, 0f), consumer, options)
            currentOffset.y += itemHeight + ENTRY_Y_MARGIN
        }

        if (listItems.size > maxVisibleEntries) {
            val scrollbarX = offset.x + (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2 + elementWidth + SCROLLBAR_MARGIN
            val trackElement = ColorElement(guiRenderer, Vec2f(SCROLLBAR_WIDTH, listHeight + ENTRY_Y_MARGIN), SCROLLBAR_TRACK_COLOR)
            trackElement.render(Vec2f(scrollbarX, listStartY), consumer, options)
            val maxScroll = listItems.size - maxVisibleEntries
            val thumbHeight = maxOf(SCROLLBAR_MIN_THUMB_HEIGHT, (listHeight + ENTRY_Y_MARGIN) * maxVisibleEntries / listItems.size)
            val thumbTravel = listHeight + ENTRY_Y_MARGIN - thumbHeight
            val thumbY = listStartY + (thumbTravel * scrollOffset / maxScroll)
            
            val thumbElement = ColorElement(guiRenderer, Vec2f(SCROLLBAR_WIDTH, thumbHeight), SCROLLBAR_THUMB_COLOR)
            thumbElement.render(Vec2f(scrollbarX, thumbY), consumer, options)
        }

        currentOffset.y += SPACING - ENTRY_Y_MARGIN

        val buttonY = currentOffset.y
        resetButton.render(currentOffset.unsafe + Vec2f((elementWidth / 2 - resetButton.size.x) / 2, 0f), consumer, options)
        doneButton.render(Vec2f(currentOffset.x + elementWidth / 2 + (elementWidth / 2 - doneButton.size.x) / 2, buttonY), consumer, options)
    }

    override fun onScroll(position: Vec2f, scrollOffset: Vec2f): Boolean {
        if (editingEntry != null) return true
        
        val maxScroll = maxOf(0, listItems.size - maxVisibleEntries)
        this.scrollOffset = (this.scrollOffset - scrollOffset.y.toInt()).coerceIn(0, maxScroll)
        cacheUpToDate = false
        
        // Recalculate hover state after scrolling since elements move under the cursor.
        val hit = getAt(position)
        if (hit == null) {
            activeElement?.onMouseLeave()
            activeElement = null
        } else {
            val (element, delta) = hit
            if (element != activeElement) {
                activeElement?.onMouseLeave()
                element.onMouseEnter(delta, position)
                activeElement = element
            } else {
                element.onMouseMove(delta, position)
            }
        }
        
        return true
    }

    override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (editingEntry != null && action == MouseActions.PRESS) {
            val keyCode = when (button) {
                MouseButtons.LEFT -> KeyCodes.MOUSE_BUTTON_LEFT
                MouseButtons.RIGHT -> KeyCodes.MOUSE_BUTTON_RIGHT
                MouseButtons.MIDDLE -> KeyCodes.MOUSE_BUTTON_MIDDLE
                MouseButtons.BUTTON_1 -> KeyCodes.MOUSE_BUTTON_1
                MouseButtons.BUTTON_2 -> KeyCodes.MOUSE_BUTTON_2
                MouseButtons.BUTTON_3 -> KeyCodes.MOUSE_BUTTON_3
                MouseButtons.BUTTON_4 -> KeyCodes.MOUSE_BUTTON_4
                MouseButtons.BUTTON_5 -> KeyCodes.MOUSE_BUTTON_5
                MouseButtons.BUTTON_6 -> KeyCodes.MOUSE_BUTTON_6
                MouseButtons.BUTTON_7 -> KeyCodes.MOUSE_BUTTON_7
                MouseButtons.BUTTON_8 -> KeyCodes.MOUSE_BUTTON_8
                MouseButtons.LAST -> KeyCodes.MOUSE_BUTTON_LAST
            }
            stopEditing(listOf(keyCode))
            return true
        }
        
        if (sensitivitySlider.isCapturingMouse) {
            val screenSize = size
            val elementWidth = calculateElementWidth()
            val startX = (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2
            val sliderX = position.x - startX - (elementWidth - sensitivitySlider.size.x) / 2
            if (sensitivitySlider.onMouseActionOutside(sliderX, button, action)) {
                return true
            }
        }
        
        if (button == MouseButtons.LEFT && listItems.size > maxVisibleEntries) {
            val scrollbarHit = getScrollbarHitArea(position)
            if (scrollbarHit != null) {
                if (action == MouseActions.PRESS) {
                    isDraggingScrollbar = true
                    dragStartY = position.y
                    dragStartScrollOffset = scrollOffset
                    return true
                }
            }
        }
        
        if (action == MouseActions.RELEASE && isDraggingScrollbar) {
            isDraggingScrollbar = false
            return true
        }
        
        val (element, delta) = getAt(position) ?: return true
        
        element.onMouseAction(delta, button, action, count)
        return true
    }

    override fun onMouseEnter(position: Vec2f, absolute: Vec2f): Boolean {
        val (element, delta) = getAt(position) ?: return true
        element.onMouseEnter(delta, absolute)
        activeElement = element
        return true
    }

    override fun onMouseMove(position: Vec2f, absolute: Vec2f): Boolean {
        if (isDraggingScrollbar) {
            val screenSize = size
            val elementWidth = calculateElementWidth()
            val visibleCount = minOf(maxVisibleEntries, listItems.size)
            val listHeight = visibleCount * (ENTRY_HEIGHT + ENTRY_Y_MARGIN) - ENTRY_Y_MARGIN
            
            val maxScroll = listItems.size - maxVisibleEntries
            val thumbHeight = maxOf(SCROLLBAR_MIN_THUMB_HEIGHT, (listHeight + ENTRY_Y_MARGIN) * maxVisibleEntries / listItems.size)
            val thumbTravel = listHeight + ENTRY_Y_MARGIN - thumbHeight
            
            val deltaY = position.y - dragStartY
            val scrollDelta = (deltaY / thumbTravel * maxScroll).toInt()
            scrollOffset = (dragStartScrollOffset + scrollDelta).coerceIn(0, maxScroll)
            cacheUpToDate = false
            return true
        }
        
        // Continue updating slider when it's capturing mouse
        if (sensitivitySlider.isCapturingMouse) {
            val screenSize = size
            val elementWidth = calculateElementWidth()
            val startX = (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2
            val sliderX = position.x - startX - (elementWidth - sensitivitySlider.size.x) / 2
            sensitivitySlider.onMouseMoveOutside(sliderX)
            return true
        }
        
        val (element, delta) = getAt(position) ?: run {
            activeElement?.onMouseLeave()
            activeElement = null
            return true
        }
        
        if (element != activeElement) {
            activeElement?.onMouseLeave()
            element.onMouseEnter(delta, absolute)
            activeElement = element
        } else {
            element.onMouseMove(delta, absolute)
        }
        return true
    }

    override fun onMouseLeave(): Boolean {
        activeElement?.onMouseLeave()
        activeElement = null
        isDraggingScrollbar = false
        return true
    }

    private fun getScrollbarHitArea(position: Vec2f): Vec2f? {
        if (listItems.size <= maxVisibleEntries) return null
        
        val screenSize = size
        val elementWidth = calculateElementWidth()
        val visibleCount = minOf(maxVisibleEntries, listItems.size)
        val listHeight = visibleCount * (ENTRY_HEIGHT + ENTRY_Y_MARGIN) - ENTRY_Y_MARGIN
        val totalHeight = titleElement.size.y + SPACING + 
                         sensitivitySlider.size.y + SPACING +
                         listHeight + ENTRY_Y_MARGIN +
                         SPACING + BUTTON_HEIGHT
        
        val startY = (screenSize.y - totalHeight) / 2
        val startX = (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2
        val listStartY = startY + titleElement.size.y + SPACING + sensitivitySlider.size.y + SPACING
        
        val scrollbarX = startX + elementWidth + SCROLLBAR_MARGIN
        val trackHeight = listHeight + ENTRY_Y_MARGIN
        
        if (position.x < scrollbarX || position.x >= scrollbarX + SCROLLBAR_WIDTH) return null
        if (position.y < listStartY || position.y >= listStartY + trackHeight) return null
        val maxScroll = listItems.size - maxVisibleEntries
        val thumbHeight = maxOf(SCROLLBAR_MIN_THUMB_HEIGHT, trackHeight * maxVisibleEntries / listItems.size)
        val thumbTravel = trackHeight - thumbHeight
        val thumbY = listStartY + (thumbTravel * scrollOffset / maxScroll)
        return Vec2f(position.x - scrollbarX, position.y - thumbY)
    }

    override fun getAt(position: Vec2f): Pair<Element, Vec2f>? {
        val screenSize = size
        val elementWidth = calculateElementWidth()
        
        val visibleCount = minOf(maxVisibleEntries, listItems.size)
        val listHeight = visibleCount * (ENTRY_HEIGHT + ENTRY_Y_MARGIN) - ENTRY_Y_MARGIN
        val totalHeight = titleElement.size.y + SPACING + 
                         sensitivitySlider.size.y + SPACING +
                         listHeight + ENTRY_Y_MARGIN +
                         SPACING + BUTTON_HEIGHT
        
        val startY = (screenSize.y - totalHeight) / 2
        val startX = (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2
        
        if (position.x < startX || position.x >= startX + elementWidth) {
            return null
        }
        
        var currentY = startY + titleElement.size.y + SPACING
        
        if (position.y >= currentY && position.y < currentY + sensitivitySlider.size.y) {
            val delta = Vec2f(position.x - startX - (elementWidth - sensitivitySlider.size.x) / 2, position.y - currentY)
            if (delta.x >= 0 && delta.x < sensitivitySlider.size.x) {
                return Pair(sensitivitySlider, delta)
            }
        }
        currentY += sensitivitySlider.size.y + SPACING
        
        val startIndex = scrollOffset
        val endIndex = minOf(startIndex + maxVisibleEntries, listItems.size)
        
        for (i in startIndex until endIndex) {
            val item = listItems[i]
            val itemHeight = if (item is CategoryHeader) CATEGORY_HEIGHT else ENTRY_HEIGHT
            
            if (position.y >= currentY && position.y < currentY + itemHeight) {
                if (item is KeyBindingEntry) {
                    val delta = Vec2f(position.x - startX - (elementWidth - item.size.x) / 2, position.y - currentY)
                    if (delta.x >= 0 && delta.x < item.size.x) {
                        return Pair(item, delta)
                    }
                }
            }
            currentY += itemHeight + ENTRY_Y_MARGIN
        }
        
        currentY += SPACING - ENTRY_Y_MARGIN
        
        if (position.y >= currentY && position.y < currentY + BUTTON_HEIGHT) {
            val resetX = startX + (elementWidth / 2 - resetButton.size.x) / 2
            if (position.x >= resetX && position.x < resetX + resetButton.size.x) {
                val delta = Vec2f(position.x - resetX, position.y - currentY)
                return Pair(resetButton, delta)
            }
            
            val doneX = startX + elementWidth / 2 + (elementWidth / 2 - doneButton.size.x) / 2
            if (position.x >= doneX && position.x < doneX + doneButton.size.x) {
                val delta = Vec2f(position.x - doneX, position.y - currentY)
                return Pair(doneButton, delta)
            }
        }
        
        return null
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (editingEntry != null) {
            if (key == KeyCodes.KEY_ESCAPE && type == KeyChangeTypes.PRESS) {
                stopEditing(emptyList())
                return true
            }
            
            when (type) {
                KeyChangeTypes.PRESS -> {
                    pressedKeys += key
                    if (pressedKeys.size > peakPressedKeys.size) {
                        peakPressedKeys = pressedKeys.toList()
                    }
                    editingEntry?.updatePressedKeys(pressedKeys)
                    cacheUpToDate = false
                }
                KeyChangeTypes.RELEASE -> {
                    if (pressedKeys.isNotEmpty()) {
                        pressedKeys -= key
                        if (pressedKeys.isEmpty()) {
                            stopEditing(peakPressedKeys)
                        } else {
                            editingEntry?.updatePressedKeys(pressedKeys)
                        }
                    }
                }
                else -> {}
            }
            return true
        }
        
        if (type != KeyChangeTypes.RELEASE) {
            when (key) {
                KeyCodes.KEY_UP -> {
                    if (scrollOffset > 0) {
                        scrollOffset--
                        cacheUpToDate = false
                    }
                    return true
                }
                KeyCodes.KEY_DOWN -> {
                    val maxScroll = maxOf(0, listItems.size - maxVisibleEntries)
                    if (scrollOffset < maxScroll) {
                        scrollOffset++
                        cacheUpToDate = false
                    }
                    return true
                }
                else -> {}
            }
        }
        activeElement?.onKey(key, type)
        return true
    }

    override fun onChildChange(child: Element) {
        cacheUpToDate = false
    }

    override fun tick() {
        super.tick()
        titleElement.tick()
        doneButton.tick()
        resetButton.tick()
        sensitivitySlider.tick()
        for (item in listItems) {
            if (item is KeyBindingEntry) {
                item.tick()
            }
        }
    }

    sealed interface ListItem
    
    class CategoryHeader(
        guiRenderer: GUIRenderer,
        val categoryKey: String,
    ) : Element(guiRenderer), ListItem {
        
        private val textElement = TextElement(guiRenderer, getCategoryDisplayName(categoryKey), background = null, parent = this).apply {
            prefMaxSize = Vec2f(-1.0f, CATEGORY_HEIGHT)
        }
        
        // Store the full width for centering
        private var containerWidth: Float = 0f
        
        fun setContainerWidth(width: Float) {
            containerWidth = width
        }
        
        override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
            val textX = if (containerWidth > 0) (containerWidth - textElement.size.x) / 2 else 0f
            textElement.render(offset + Vec2f(textX, (CATEGORY_HEIGHT - textElement.size.y) / 2), consumer, options)
        }
        
        override fun forceSilentApply() {
            textElement.silentApply()
            size = Vec2f(if (containerWidth > 0) containerWidth else textElement.size.x, CATEGORY_HEIGHT)
        }
        
        override fun onChildChange(child: Element) {
            cacheUpToDate = false
        }
        
        companion object {
            fun getCategoryDisplayName(categoryKey: String): String {
                val translationKey = "minosoft:key.category.$categoryKey"
                val translated = IntegratedLanguage.LANGUAGE.forceTranslate(translationKey.i18n().translationKey).message
                return if (translated != translationKey) {
                    translated
                } else {
                    categoryKey.replace('_', ' ').replaceFirstChar { it.uppercase() }
                }
            }
        }
    }

    class KeyBindingEntry(
        guiRenderer: GUIRenderer,
        val bindingName: ResourceLocation,
        private var binding: KeyBinding,
        val defaultBinding: KeyBinding,
        private val menu: ControlsSettingsMenu,
    ) : Element(guiRenderer), ListItem {
        
        private val nameElement = TextElement(guiRenderer, getDisplayName(bindingName), background = null, parent = this)
        private val keyButton = ButtonElement(guiRenderer, getKeyDisplayText(binding)) { 
            menu.startEditing(this) 
        }.apply { parent = this@KeyBindingEntry }
        
        var isEditing: Boolean = false
            set(value) {
                field = value
                updateKeyButtonText()
            }
        
        private var currentPressedKeys: Set<KeyCodes> = emptySet()
        
        init {
            updateKeyButtonText()
        }
        
        fun updateBinding(newBinding: KeyBinding) {
            binding = newBinding
            updateKeyButtonText()
        }
        
        fun updatePressedKeys(keys: Set<KeyCodes>) {
            currentPressedKeys = keys
            if (isEditing) {
                keyButton.textElement.text = if (keys.isEmpty()) {
                    "> ??? <"
                } else {
                    "> ${keys.joinToString(" + ") { it.keyName }} <"
                }
            }
        }
        
        private fun updateKeyButtonText() {
            keyButton.textElement.text = if (isEditing) {
                if (currentPressedKeys.isEmpty()) {
                    "> ??? <"
                } else {
                    "> ${currentPressedKeys.joinToString(" + ") { it.keyName }} <"
                }
            } else {
                currentPressedKeys = emptySet()
                getKeyDisplayText(binding)
            }
        }
        
        override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
            val entrySize = size
            val halfWidth = entrySize.x / 2
            
            // Render name on the left, button on the right
            nameElement.render(offset + Vec2f(4f, (entrySize.y - nameElement.size.y) / 2), consumer, options)
            keyButton.size = Vec2f(halfWidth - 8f, entrySize.y - 2f)
            keyButton.render(offset + Vec2f(halfWidth + 4f, 1f), consumer, options)
        }
        
        override fun forceSilentApply() {
            nameElement.silentApply()
            keyButton.silentApply()
            cacheUpToDate = false
        }

        override fun onChildChange(child: Element) {
            cacheUpToDate = false
        }
        
        override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
            val halfWidth = size.x / 2
            if (position.x >= halfWidth) {
                val delta = Vec2f(position.x - halfWidth - 4f, position.y - 1f)
                return keyButton.onMouseAction(delta, button, action, count)
            }
            return true
        }
        
        override fun onMouseEnter(position: Vec2f, absolute: Vec2f): Boolean {
            val halfWidth = size.x / 2
            if (position.x >= halfWidth) {
                keyButton.onMouseEnter(Vec2f(position.x - halfWidth - 4f, position.y - 1f), absolute)
            }
            return true
        }
        
        override fun onMouseMove(position: Vec2f, absolute: Vec2f): Boolean {
            val halfWidth = size.x / 2
            if (position.x >= halfWidth) {
                keyButton.onMouseEnter(Vec2f(position.x - halfWidth - 4f, position.y - 1f), absolute)
            } else {
                keyButton.onMouseLeave()
            }
            return true
        }
        
        override fun onMouseLeave(): Boolean {
            keyButton.onMouseLeave()
            return true
        }
        
        override fun tick() {
            nameElement.tick()
            keyButton.tick()
        }
        
        companion object {
            fun getDisplayName(name: ResourceLocation): String {
                // Try to get translated name using the key.* pattern, fallback to formatted path if no translation found.
                val translationKey = "minosoft:key.${name.path}"
                val translated = IntegratedLanguage.LANGUAGE.forceTranslate(translationKey.i18n().translationKey).message
                return if (translated != translationKey) {
                    translated
                } else {
                    name.path.replace('_', ' ').replaceFirstChar { it.uppercase() }
                }
            }
            
            fun getKeyDisplayText(binding: KeyBinding): String {
                val keys = mutableListOf<String>()
                
                for ((action, codes) in binding.action) {
                    for (code in codes) {
                        keys += code.keyName
                    }
                }
                
                return if (keys.isEmpty()) {
                    "NONE"
                } else {
                    keys.joinToString(" + ")
                }
            }
        }
    }

    companion object : GUIBuilder<LayoutedGUIElement<ControlsSettingsMenu>> {
        private const val WIDTH_PERCENTAGE = 0.35f
        private const val MIN_BUTTON_WIDTH = 200.0f
        private const val ENTRY_Y_MARGIN = 3.0f
        private const val ENTRY_HEIGHT = 22.0f
        private const val CATEGORY_HEIGHT = 18.0f
        private const val BUTTON_HEIGHT = 20.0f
        private const val SPACING = 10.0f
        
        private const val SCROLLBAR_WIDTH = 6.0f
        private const val SCROLLBAR_MARGIN = 4.0f
        private const val SCROLLBAR_MIN_THUMB_HEIGHT = 20.0f
        private val SCROLLBAR_TRACK_COLOR = RGBAColor(40, 40, 40, 180)
        private val SCROLLBAR_THUMB_COLOR = RGBAColor(120, 120, 120, 220)
        
        val CATEGORIZED_KEYBINDINGS: Map<String, Map<ResourceLocation, KeyBinding>> = linkedMapOf(
            // Movement category
            "movement" to linkedMapOf(
                minosoft("move_forward") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_W)),
                minosoft("move_backwards") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_S)),
                minosoft("move_left") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_A)),
                minosoft("move_right") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_D)),
                minosoft("move_jump") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_SPACE)),
                minosoft("move_sneak") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_LEFT_SHIFT)),
                minosoft("move_sprint") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_LEFT_CONTROL)),
            ),
            
            // Gameplay category
            "gameplay" to linkedMapOf(
                minosoft("attack") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.MOUSE_BUTTON_LEFT)),
                minosoft("use_item") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.MOUSE_BUTTON_RIGHT)),
                minosoft("pick_item") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.MOUSE_BUTTON_MIDDLE)),
                minosoft("drop_item") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_Q)),
                minosoft("swap_items") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_F)),
                minosoft("local_inventory") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_E)),
                minosoft("stop_spectating") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_LEFT_SHIFT)),
            ),
            
            // Hotbar category
            "hotbar" to linkedMapOf(
                minosoft("hotbar_slot_1") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_1)),
                minosoft("hotbar_slot_2") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_2)),
                minosoft("hotbar_slot_3") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_3)),
                minosoft("hotbar_slot_4") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_4)),
                minosoft("hotbar_slot_5") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_5)),
                minosoft("hotbar_slot_6") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_6)),
                minosoft("hotbar_slot_7") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_7)),
                minosoft("hotbar_slot_8") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_8)),
                minosoft("hotbar_slot_9") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_9)),
            ),
            
            // Camera category
            "camera" to linkedMapOf(
                minosoft("zoom") to KeyBinding(KeyActions.CHANGE to setOf(KeyCodes.KEY_C)),
                minosoft("camera_third_person") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_F5)),
            ),
            
            // Miscellaneous category
            "miscellaneous" to linkedMapOf(
                minosoft("take_screenshot") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_F2)),
                minosoft("toggle_fullscreen") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_F11)),
                minosoft("open_chat") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_T)),
                minosoft("open_command") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_SLASH)),
                minosoft("enable_hud") to KeyBinding(KeyActions.PRESS to setOf(KeyCodes.KEY_F1)),
            ),
            
            // Debug category
            "debug" to linkedMapOf(
                minosoft("debug_polygon") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_P)),
                minosoft("cursor_mode") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_M)),
                minosoft("pause_incoming") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_I)),
                minosoft("pause_outgoing") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_O)),
                minosoft("camera_debug_view") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_V)),
                minosoft("toggle_hitboxes") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3), KeyActions.PRESS to setOf(KeyCodes.KEY_B)),
                minosoft("chunk_border") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3), KeyActions.PRESS to setOf(KeyCodes.KEY_G)),
                minosoft("clear_chunk_cache") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F3), KeyActions.PRESS to setOf(KeyCodes.KEY_A)),
                minosoft("recalculate_light") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_A)),
                minosoft("fullbright") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_C)),
                minosoft("switch_fun_effects") to KeyBinding(KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4), KeyActions.PRESS to setOf(KeyCodes.KEY_J)),
            ),
        )

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ControlsSettingsMenu> {
            return LayoutedGUIElement(ControlsSettingsMenu(guiRenderer))
        }
    }
}
