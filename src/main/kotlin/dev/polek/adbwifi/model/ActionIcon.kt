package dev.polek.adbwifi.model

import com.intellij.icons.AllIcons
import com.intellij.openapi.diagnostic.logger
import javax.swing.Icon

data class ActionIcon(
    val id: String,
    val displayName: String,
    val icon: Icon
)

object ActionIconsProvider {

    private val LOG = logger<ActionIconsProvider>()

    private val icons: List<ActionIcon> by lazy { discoverAllIcons() }
    private val iconsByIdMap: Map<String, ActionIcon> by lazy {
        icons.associateBy { it.id.lowercase() }
    }

    private fun discoverAllIcons(): List<ActionIcon> {
        val result = mutableListOf<ActionIcon>()
        discoverIconsRecursively(AllIcons::class.java, "", result)
        return result.sortedBy { it.displayName.lowercase() }
    }

    private fun discoverIconsRecursively(
        clazz: Class<*>,
        prefix: String,
        result: MutableList<ActionIcon>
    ) {
        // Get all Icon fields from this class
        clazz.declaredFields
            .filter { Icon::class.java.isAssignableFrom(it.type) }
            .forEach { field -> loadIconField(field, prefix, result) }

        // Recurse into nested classes
        clazz.declaredClasses.forEach { nestedClass ->
            val nestedPrefix = if (prefix.isEmpty()) {
                nestedClass.simpleName
            } else {
                "$prefix.${nestedClass.simpleName}"
            }
            discoverIconsRecursively(nestedClass, nestedPrefix, result)
        }
    }

    private fun loadIconField(
        field: java.lang.reflect.Field,
        prefix: String,
        result: MutableList<ActionIcon>
    ) {
        try {
            field.isAccessible = true
            val icon = field.get(null) as? Icon ?: return
            val id = if (prefix.isEmpty()) field.name else "$prefix.${field.name}"
            val displayName = buildDisplayName(prefix, field.name)
            result.add(ActionIcon(id, displayName, icon))
        } catch (e: IllegalAccessException) {
            LOG.debug("Cannot access icon field: $prefix.${field.name}", e)
        } catch (e: SecurityException) {
            LOG.debug("Security exception loading icon: $prefix.${field.name}", e)
        }
    }

    private fun buildDisplayName(prefix: String, fieldName: String): String {
        // Convert camelCase/PascalCase to readable format
        val readableName = fieldName
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }

        return if (prefix.isEmpty()) {
            readableName
        } else {
            "$prefix / $readableName"
        }
    }

    fun getIconById(id: String): ActionIcon? = iconsByIdMap[id.lowercase()]

    fun search(query: String): List<ActionIcon> {
        if (query.isBlank()) return icons
        val lowerQuery = query.lowercase()
        return icons.filter {
            it.displayName.lowercase().contains(lowerQuery) ||
                it.id.lowercase().contains(lowerQuery)
        }
    }
}
