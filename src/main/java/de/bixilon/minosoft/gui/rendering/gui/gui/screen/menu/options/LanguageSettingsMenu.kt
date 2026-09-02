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
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.language.LanguageUtil
import de.bixilon.minosoft.data.language.LanguageUtil.i18n
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
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

class LanguageSettingsMenu(guiRenderer: GUIRenderer) : Screen(guiRenderer), AbstractLayout<Element> {
    private val erosProfile = guiRenderer.context.session.profiles.eros.general
    private val currentLanguage: String get() = erosProfile.language

    private val titleElement = TextElement(guiRenderer, "menu.options.language.title".i18n(), background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 2.0f))
    private val doneButton = ButtonElement(guiRenderer, "menu.options.done".i18n()) { guiRenderer.gui.pop() }.apply { parent = this@LanguageSettingsMenu }

    private val languageButtons: MutableList<LanguageButtonElement> = mutableListOf()
    private var scrollOffset = 0
    private val maxVisibleLanguages = 6

    private var isDraggingScrollbar = false
    private var dragStartY = 0f
    private var dragStartScrollOffset = 0

    override var activeElement: Element? = null
    override var activeDragElement: Element? = null

    init {
        for (language in AVAILABLE_LANGUAGES) {
            val button = LanguageButtonElement(guiRenderer, language, language == currentLanguage) {
                selectLanguage(language)
            }
            button.parent = this
            languageButtons += button
        }
        forceSilentApply()
    }

    private fun selectLanguage(language: String) {
        erosProfile.language = language
        IntegratedLanguage.load(language)
        
        // Update all button states
        for (button in languageButtons) {
            button.isSelected = button.languageCode == language
        }
        
        // Update title and done button text
        titleElement.text = "menu.options.language.title".i18n()
        doneButton.textElement.text = "menu.options.done".i18n()
    }

    private fun calculateElementWidth(): Float {
        return maxOf(size.x * WIDTH_PERCENTAGE, MIN_BUTTON_WIDTH)
    }

    override fun forceSilentApply() {
        titleElement.silentApply() // Ensure title size is calculated first
        val elementWidth = calculateElementWidth()
        
        titleElement.prefMaxSize = Vec2f(elementWidth, -1f)
        doneButton.size = Vec2f(elementWidth, doneButton.size.y)
        
        for (button in languageButtons) {
            button.size = Vec2f(elementWidth, button.size.y)
        }
        
        super.forceSilentApply()
        cacheUpToDate = false
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        val screenSize = size
        val elementWidth = calculateElementWidth()
        val currentOffset = MVec2f(offset)

        val visibleCount = minOf(maxVisibleLanguages, languageButtons.size)
        val listHeight = visibleCount * (BUTTON_HEIGHT + BUTTON_Y_MARGIN) - BUTTON_Y_MARGIN
        val totalHeight = titleElement.size.y + SPACING + 
                         listHeight + BUTTON_Y_MARGIN +
                         SPACING + doneButton.size.y
        
        currentOffset.y += (screenSize.y - totalHeight) / 2
        currentOffset.x += (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2

        titleElement.render(currentOffset.unsafe + Vec2f((elementWidth - titleElement.size.x) / 2, 0f), consumer, options)
        currentOffset.y += titleElement.size.y + SPACING

        val listStartY = currentOffset.y

        val startIndex = scrollOffset
        val endIndex = minOf(startIndex + maxVisibleLanguages, languageButtons.size)
        
        for (i in startIndex until endIndex) {
            val button = languageButtons[i]
            button.render(currentOffset.unsafe + Vec2f((elementWidth - button.size.x) / 2, 0f), consumer, options)
            currentOffset.y += BUTTON_HEIGHT + BUTTON_Y_MARGIN
        }

        if (languageButtons.size > maxVisibleLanguages) {
            val scrollbarX = offset.x + (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2 + elementWidth + SCROLLBAR_MARGIN
            val trackElement = ColorElement(guiRenderer, Vec2f(SCROLLBAR_WIDTH, listHeight + BUTTON_Y_MARGIN), SCROLLBAR_TRACK_COLOR)
            trackElement.render(Vec2f(scrollbarX, listStartY), consumer, options)
            val maxScroll = languageButtons.size - maxVisibleLanguages
            val thumbHeight = maxOf(SCROLLBAR_MIN_THUMB_HEIGHT, (listHeight + BUTTON_Y_MARGIN) * maxVisibleLanguages / languageButtons.size)
            val thumbTravel = listHeight + BUTTON_Y_MARGIN - thumbHeight
            val thumbY = listStartY + (thumbTravel * scrollOffset / maxScroll)
            
            val thumbElement = ColorElement(guiRenderer, Vec2f(SCROLLBAR_WIDTH, thumbHeight), SCROLLBAR_THUMB_COLOR)
            thumbElement.render(Vec2f(scrollbarX, thumbY), consumer, options)
        }

        currentOffset.y += SPACING - BUTTON_Y_MARGIN

        doneButton.render(currentOffset.unsafe + Vec2f((elementWidth - doneButton.size.x) / 2, 0f), consumer, options)
    }

    override fun onScroll(position: Vec2f, scrollOffset: Vec2f): Boolean {
        val maxScroll = maxOf(0, languageButtons.size - maxVisibleLanguages)
        this.scrollOffset = (this.scrollOffset - scrollOffset.y.toInt()).coerceIn(0, maxScroll)
        cacheUpToDate = false
        return true
    }

    override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (button == MouseButtons.LEFT && languageButtons.size > maxVisibleLanguages) {
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
            val visibleCount = minOf(maxVisibleLanguages, languageButtons.size)
            val listHeight = visibleCount * (BUTTON_HEIGHT + BUTTON_Y_MARGIN) - BUTTON_Y_MARGIN
            
            val maxScroll = languageButtons.size - maxVisibleLanguages
            val thumbHeight = maxOf(SCROLLBAR_MIN_THUMB_HEIGHT, (listHeight + BUTTON_Y_MARGIN) * maxVisibleLanguages / languageButtons.size)
            val thumbTravel = listHeight + BUTTON_Y_MARGIN - thumbHeight
            
            val deltaY = position.y - dragStartY
            val scrollDelta = (deltaY / thumbTravel * maxScroll).toInt()
            scrollOffset = (dragStartScrollOffset + scrollDelta).coerceIn(0, maxScroll)
            cacheUpToDate = false
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
        }
        return true
    }

    override fun onMouseLeave(): Boolean {
        activeElement?.onMouseLeave()
        activeElement = null
        isDraggingScrollbar = false
        return true
    }

    /**
     * Check if position is within the scrollbar thumb area
     * Returns the relative position within the scrollbar if hit, null otherwise
     * Couldn't find a better way for this.
     */
    private fun getScrollbarHitArea(position: Vec2f): Vec2f? {
        if (languageButtons.size <= maxVisibleLanguages) return null
        
        val screenSize = size
        val elementWidth = calculateElementWidth()
        val visibleCount = minOf(maxVisibleLanguages, languageButtons.size)
        val listHeight = visibleCount * (BUTTON_HEIGHT + BUTTON_Y_MARGIN) - BUTTON_Y_MARGIN
        val totalHeight = titleElement.size.y + SPACING + 
                         listHeight + BUTTON_Y_MARGIN +
                         SPACING + doneButton.size.y
        
        val startY = (screenSize.y - totalHeight) / 2
        val startX = (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2
        val listStartY = startY + titleElement.size.y + SPACING
        
        val scrollbarX = startX + elementWidth + SCROLLBAR_MARGIN
        val trackHeight = listHeight + BUTTON_Y_MARGIN
        
        if (position.x < scrollbarX || position.x >= scrollbarX + SCROLLBAR_WIDTH) return null
        if (position.y < listStartY || position.y >= listStartY + trackHeight) return null
        val maxScroll = languageButtons.size - maxVisibleLanguages
        val thumbHeight = maxOf(SCROLLBAR_MIN_THUMB_HEIGHT, trackHeight * maxVisibleLanguages / languageButtons.size)
        val thumbTravel = trackHeight - thumbHeight
        val thumbY = listStartY + (thumbTravel * scrollOffset / maxScroll)
        return Vec2f(position.x - scrollbarX, position.y - thumbY)
    }

    override fun getAt(position: Vec2f): Pair<Element, Vec2f>? {
        val screenSize = size
        val elementWidth = calculateElementWidth()
        
        val visibleCount = minOf(maxVisibleLanguages, languageButtons.size)
        val listHeight = visibleCount * (BUTTON_HEIGHT + BUTTON_Y_MARGIN) - BUTTON_Y_MARGIN
        val totalHeight = titleElement.size.y + SPACING + 
                         listHeight + BUTTON_Y_MARGIN +
                         SPACING + doneButton.size.y
        
        val startY = (screenSize.y - totalHeight) / 2
        val startX = (screenSize.x - elementWidth - SCROLLBAR_WIDTH - SCROLLBAR_MARGIN) / 2
        
        if (position.x < startX || position.x >= startX + elementWidth) {
            return null
        }
        
        var currentY = startY + titleElement.size.y + SPACING
        
        val startIndex = scrollOffset
        val endIndex = minOf(startIndex + maxVisibleLanguages, languageButtons.size)
        
        for (i in startIndex until endIndex) {
            if (position.y >= currentY && position.y < currentY + BUTTON_HEIGHT) {
                val button = languageButtons[i]
                val delta = Vec2f(position.x - startX - (elementWidth - button.size.x) / 2, position.y - currentY)
                if (delta.x >= 0 && delta.x < button.size.x) {
                    return Pair(button, delta)
                }
            }
            currentY += BUTTON_HEIGHT + BUTTON_Y_MARGIN
        }
        
        currentY += SPACING - BUTTON_Y_MARGIN
        
        if (position.y >= currentY && position.y < currentY + doneButton.size.y) {
            val delta = Vec2f(position.x - startX - (elementWidth - doneButton.size.x) / 2, position.y - currentY)
            if (delta.x >= 0 && delta.x < doneButton.size.x) {
                return Pair(doneButton, delta)
            }
        }
        
        return null
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
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
                    val maxScroll = maxOf(0, languageButtons.size - maxVisibleLanguages)
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
        for (button in languageButtons) {
            button.tick()
        }
    }

    private class LanguageButtonElement(
        guiRenderer: GUIRenderer,
        val languageCode: String,
        isSelected: Boolean,
        onSubmit: () -> Unit,
    ) : ButtonElement(guiRenderer, getDisplayName(languageCode), false, onSubmit) {
        
        var isSelected: Boolean = isSelected
            set(value) {
                if (field != value) {
                    field = value
                    updateText()
                }
            }
        
        init {
            updateText()
        }
        
        private fun updateText() {
            textElement.text = if (isSelected) {
                "» ${getDisplayName(languageCode)} «"
            } else {
                getDisplayName(languageCode)
            }
        }
        
        override var size: Vec2f
            get() = Vec2f(super.size.x, BUTTON_HEIGHT)
            set(value) { super.size = value }

        companion object {
            fun getDisplayName(code: String): String {
                return LANGUAGE_NAMES[code] ?: code
            }
        }
    }

    companion object : GUIBuilder<LayoutedGUIElement<LanguageSettingsMenu>> {
        private const val WIDTH_PERCENTAGE = 0.25f  // 25% of window width
        private const val MIN_BUTTON_WIDTH = 150.0f
        private const val BUTTON_Y_MARGIN = 5.0f
        private const val BUTTON_HEIGHT = 20.0f
        private const val SPACING = 10.0f
        
        private const val SCROLLBAR_WIDTH = 6.0f
        private const val SCROLLBAR_MARGIN = 4.0f
        private const val SCROLLBAR_MIN_THUMB_HEIGHT = 20.0f
        private val SCROLLBAR_TRACK_COLOR = RGBAColor(40, 40, 40, 180)
        private val SCROLLBAR_THUMB_COLOR = RGBAColor(120, 120, 120, 220)

        // Available languages - all 137 Minecraft languages, not sure if theres a way to dynamically get them from files of game.
        val AVAILABLE_LANGUAGES = listOf(
            "af_za",    // Afrikaans
            "ar_sa",    // Arabic
            "ast_es",   // Asturian
            "az_az",    // Azerbaijani
            "ba_ru",    // Bashkir
            "bar",      // Bavarian
            "be_by",    // Belarusian (Cyrillic)
            "be_latn",  // Belarusian (Latin)
            "bg_bg",    // Bulgarian
            "br_fr",    // Breton
            "brb",      // Brabantian
            "bs_ba",    // Bosnian
            "ca_es",    // Catalan
            "cs_cz",    // Czech
            "cy_gb",    // Welsh
            "da_dk",    // Danish
            "de_at",    // Austrian German
            "de_ch",    // Swiss German
            "de_de",    // German
            "el_gr",    // Greek
            "en_au",    // Australian English
            "en_ca",    // Canadian English
            "en_gb",    // British English
            "en_nz",    // New Zealand English
            "en_pt",    // Pirate Speak
            "en_ud",    // Upside down English
            LanguageUtil.FALLBACK_LANGUAGE,  // en_us - American English
            "enp",      // Anglish
            "enws",     // Shakespearean English
            "eo_uy",    // Esperanto
            "es_ar",    // Argentinian Spanish
            "es_cl",    // Chilean Spanish
            "es_ec",    // Ecuadorian Spanish
            "es_es",    // European Spanish
            "es_mx",    // Mexican Spanish
            "es_uy",    // Uruguayan Spanish
            "es_ve",    // Venezuelan Spanish
            "esan",     // Andalusian
            "et_ee",    // Estonian
            "eu_es",    // Basque
            "fa_ir",    // Persian
            "fi_fi",    // Finnish
            "fil_ph",   // Filipino
            "fo_fo",    // Faroese
            "fr_ca",    // Canadian French
            "fr_fr",    // European French
            "fra_de",   // East Franconian
            "fur_it",   // Friulian
            "fy_nl",    // Frisian
            "ga_ie",    // Irish
            "gd_gb",    // Scottish Gaelic
            "gl_es",    // Galician
            "hal_ua",   // Halychian
            "haw_us",   // Hawaiian
            "he_il",    // Hebrew
            "hi_in",    // Hindi
            "hn_no",    // High Norwegian
            "hr_hr",    // Croatian
            "hu_hu",    // Hungarian
            "hy_am",    // Armenian
            "id_id",    // Indonesian
            "ig_ng",    // Igbo
            "io_en",    // Ido
            "is_is",    // Icelandic
            "isv",      // Interslavic
            "it_it",    // Italian
            "ja_jp",    // Japanese
            "jbo_en",   // Lojban
            "ka_ge",    // Georgian
            "kk_kz",    // Kazakh
            "kn_in",    // Kannada
            "ko_kr",    // Korean
            "ksh",      // Kölsch/Ripuarian
            "kw_gb",    // Cornish
            "ky_kg",    // Kyrgyz
            "la_la",    // Latin
            "lb_lu",    // Luxembourgish
            "li_li",    // Limburgish
            "lmo",      // Lombard
            "lo_la",    // Lao
            "lol_us",   // LOLCAT
            "lt_lt",    // Lithuanian
            "lv_lv",    // Latvian
            "lzh",      // Literary Chinese
            "mk_mk",    // Macedonian
            "mn_mn",    // Mongolian
            "ms_my",    // Malay
            "mt_mt",    // Maltese
            "nah",      // Nahuatl
            "nds_de",   // Low German
            "nl_be",    // Dutch (Flemish)
            "nl_nl",    // Dutch
            "nn_no",    // Norwegian Nynorsk
            "no_no",    // Norwegian Bokmål
            "oc_fr",    // Occitan
            "ovd",      // Elfdalian
            "pl_pl",    // Polish
            "pls",      // Popoloca
            "pt_br",    // Brazilian Portuguese
            "pt_pt",    // European Portuguese
            "qcb_es",   // Cantabrian
            "qid",      // Indonesian (Pre-reform)
            "qya_aa",   // Quenya
            "ro_ro",    // Romanian
            "rpr",      // Russian (Pre-revolutionary)
            "ru_ru",    // Russian
            "ry_ua",    // Rusyn
            "sah_sah",  // Yakut
            "se_no",    // Northern Sami
            "sk_sk",    // Slovak
            "sl_si",    // Slovenian
            "so_so",    // Somali
            "sq_al",    // Albanian
            "sr_cs",    // Serbian (Latin)
            "sr_sp",    // Serbian (Cyrillic)
            "sv_se",    // Swedish
            "sxu",      // Upper Saxon German
            "szl",      // Silesian
            "ta_in",    // Tamil
            "th_th",    // Thai
            "tl_ph",    // Tagalog
            "tlh_aa",   // Klingon
            "tok",      // Toki Pona
            "tr_tr",    // Turkish
            "tt_ru",    // Tatar
            "tzo_mx",   // Tzotzil
            "uk_ua",    // Ukrainian
            "val_es",   // Valencian
            "vec_it",   // Venetian
            "vi_vn",    // Vietnamese
            "vp_vl",    // Viossa
            "yi_de",    // Yiddish
            "yo_ng",    // Yoruba
            "zh_cn",    // Chinese Simplified
            "zh_hk",    // Chinese Traditional (Hong Kong)
            "zh_tw",    // Chinese Traditional (Taiwan)
            "zlm_arab", // Malay (Jawi)
        )

        // Language display names
        val LANGUAGE_NAMES = mapOf(
            "af_za" to "Afrikaans (Suid-Afrika)",
            "ar_sa" to "العربية (العالم العربي)",
            "ast_es" to "Asturianu (Asturies)",
            "az_az" to "Azərbaycanca (Azərbaycan)",
            "ba_ru" to "Башҡортса (Башҡортостан)",
            "bar" to "Boarisch (Bayern)",
            "be_by" to "Беларуская (Беларусь)",
            "be_latn" to "Biełaruskaja (Biełaruś)",
            "bg_bg" to "Български (България)",
            "br_fr" to "Brezhoneg (Breizh)",
            "brb" to "Braobans (Braobant)",
            "bs_ba" to "Bosanski (Bosna i Hercegovina)",
            "ca_es" to "Català (Catalunya)",
            "cs_cz" to "Čeština (Česko)",
            "cy_gb" to "Cymraeg (Cymru)",
            "da_dk" to "Dansk (Danmark)",
            "de_at" to "Deitsch (Österreich)",
            "de_ch" to "Schwiizerdutsch (Schwiiz)",
            "de_de" to "Deutsch (Deutschland)",
            "el_gr" to "Ελληνικά (Ελλάδα)",
            "en_au" to "English (Australia)",
            "en_ca" to "English (Canada)",
            "en_gb" to "English (United Kingdom)",
            "en_nz" to "English (New Zealand)",
            "en_pt" to "Pirate Speak (The Seven Seas)",
            "en_ud" to "ɥsᴉꞁᵷuƎ (uʍoᗡ ǝpᴉsd∩)",
            "en_us" to "English (US)",
            "enp" to "Anglish (Oned Riches)",
            "enws" to "Shakespearean English",
            "eo_uy" to "Esperanto (Esperantujo)",
            "es_ar" to "Español (Argentina)",
            "es_cl" to "Español (Chile)",
            "es_ec" to "Español (Ecuador)",
            "es_es" to "Español (España)",
            "es_mx" to "Español (México)",
            "es_uy" to "Español (Uruguay)",
            "es_ve" to "Español (Venezuela)",
            "esan" to "Andalûh (Andaluçía)",
            "et_ee" to "Eesti (Eesti)",
            "eu_es" to "Euskara (Euskal Herria)",
            "fa_ir" to "فارسی (ایران)",
            "fi_fi" to "Suomi (Suomi)",
            "fil_ph" to "Filipino (Pilipinas)",
            "fo_fo" to "Føroyskt (Føroyar)",
            "fr_ca" to "Français (Canada)",
            "fr_fr" to "Français (France)",
            "fra_de" to "Fränggisch (Franggn)",
            "fur_it" to "Furlan (Friûl)",
            "fy_nl" to "Frysk (Fryslân)",
            "ga_ie" to "Gaeilge (Éire)",
            "gd_gb" to "Gàidhlig (Alba)",
            "gl_es" to "Galego (Galicia)",
            "hal_ua" to "Галицка (Галичина)",
            "haw_us" to "'Ōlelo Hawai'i (Hawai'i)",
            "he_il" to "עברית (ישראל)",
            "hi_in" to "हिंदी (भारत)",
            "hn_no" to "Høgnorsk (Norig)",
            "hr_hr" to "Hrvatski (Hrvatska)",
            "hu_hu" to "Magyar (Magyarország)",
            "hy_am" to "Հայերեն (Հայաստան)",
            "id_id" to "Bahasa Indonesia (Indonesia)",
            "ig_ng" to "Igbo (Naigeria)",
            "io_en" to "Ido (Idia)",
            "is_is" to "Íslenska (Ísland)",
            "isv" to "Medžuslovjansky (Slovjanščina)",
            "it_it" to "Italiano (Italia)",
            "ja_jp" to "日本語 (日本)",
            "jbo_en" to "la .lojban. (la jbogu'e)",
            "ka_ge" to "ქართული (საქართველო)",
            "kk_kz" to "Қазақша (Қазақстан)",
            "kn_in" to "ಕನ್ನಡ (ಭಾರತ)",
            "ko_kr" to "한국어 (대한민국)",
            "ksh" to "Kölsch/Ripoarisch (Rhingland)",
            "kw_gb" to "Kernewek (Kernow)",
            "ky_kg" to "Кыргызча (Кыргызстан)",
            "la_la" to "Latina (Latium)",
            "lb_lu" to "Lëtzebuergesch (Lëtzebuerg)",
            "li_li" to "Limburgs (Limburg)",
            "lmo" to "Lombard (Lombardia)",
            "lo_la" to "ລາວ (ປະເທດລາວ)",
            "lol_us" to "LOLCAT (Kingdom of Cats)",
            "lt_lt" to "Lietuvių (Lietuva)",
            "lv_lv" to "Latviešu (Latvija)",
            "lzh" to "文言 (華夏)",
            "mk_mk" to "Македонски (Северна Македонија)",
            "mn_mn" to "Монгол (Монгол Улс)",
            "ms_my" to "Bahasa Melayu (Malaysia)",
            "mt_mt" to "Malti (Malta)",
            "nah" to "Mēxikatlahtōlli (Mēxiko)",
            "nds_de" to "Plattdüütsh (Düütschland)",
            "nl_be" to "Vlaams (België)",
            "nl_nl" to "Nederlands (Nederland)",
            "nn_no" to "Norsk nynorsk (Noreg)",
            "no_no" to "Norsk bokmål (Norge)",
            "oc_fr" to "Occitan (Occitània)",
            "ovd" to "Övdalska (Swerre)",
            "pl_pl" to "Polski (Polska)",
            "pls" to "Ngiiwa (Ndanìꞌngà)",
            "pt_br" to "Português (Brasil)",
            "pt_pt" to "Português (Portugal)",
            "qcb_es" to "Cántabru/Montañés (Cantabria)",
            "qid" to "Bahasa Indonesia edjaän lama",
            "qya_aa" to "Quenya (Arda)",
            "ro_ro" to "Română (România)",
            "rpr" to "Русскій дореформенный",
            "ru_ru" to "Русский (Россия)",
            "ry_ua" to "Руснацькый (Пудкарпатя)",
            "sah_sah" to "Сахалыы (Cаха Сирэ)",
            "se_no" to "Davvisámegiella (Sápmi)",
            "sk_sk" to "Slovenčina (Slovensko)",
            "sl_si" to "Slovenščina (Slovenija)",
            "so_so" to "Af-Soomaali (Soomaaliya)",
            "sq_al" to "Shqip (Shqiperia)",
            "sr_cs" to "Srpski (Srbija)",
            "sr_sp" to "Српски (Србија)",
            "sv_se" to "Svenska (Sverige)",
            "sxu" to "Säggs'sch (Saggsn)",
            "szl" to "Ślōnski (Gōrny Ślōnsk)",
            "ta_in" to "தமிழ் (இந்தியா)",
            "th_th" to "ไทย (ประเทศไทย)",
            "tl_ph" to "Tagalog (Pilipinas)",
            "tlh_aa" to "tlhIngan Hol (tlhIngan wo')",
            "tok" to "toki pona (ma pona)",
            "tr_tr" to "Türkçe (Türkiye)",
            "tt_ru" to "Татарча (Татарстан)",
            "tzo_mx" to "Bats'i k'op (Jobel)",
            "uk_ua" to "Українська (Україна)",
            "val_es" to "Català (Valencià)",
            "vec_it" to "Vèneto (Veneto)",
            "vi_vn" to "Tiếng Việt (Việt Nam)",
            "vp_vl" to "Viossa (Vilant)",
            "yi_de" to "ייִדיש (אשכנזיש יידן)",
            "yo_ng" to "Yorùbá (Nàìjíríà)",
            "zh_cn" to "简体中文 (中国大陆)",
            "zh_hk" to "繁體中文 (香港)",
            "zh_tw" to "繁體中文 (台灣)",
            "zlm_arab" to "بهاس ملايو (مليسيا)",
        )

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<LanguageSettingsMenu> {
            return LayoutedGUIElement(LanguageSettingsMenu(guiRenderer))
        }
    }
}
