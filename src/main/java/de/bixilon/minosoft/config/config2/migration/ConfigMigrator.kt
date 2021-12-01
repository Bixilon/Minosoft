package de.bixilon.minosoft.config.config2.migration

interface ConfigMigrator {

    fun migrate(data: MutableMap<String, Any>)
}
