package de.bixilon.minosoft.data.mappings.blocks.types

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.versions.Registries

abstract class HorizontalFacingBlock(resourceLocation: ResourceLocation, mappings: Registries, data: JsonObject) :Block(resourceLocation, mappings, data)
