package dev.polek.adbwifi.model

import com.intellij.icons.AllIcons
import java.lang.reflect.Field
import javax.swing.Icon

data class ActionIcon(
    val id: String,
    val displayName: String,
    val icon: Icon
)

object ActionIconsProvider {

    private val icons: List<ActionIcon> by lazy { loadAllActionIcons() }

    fun getIconById(id: String): ActionIcon? =
        icons.find { it.id.equals(id, ignoreCase = true) }

    fun search(query: String): List<ActionIcon> {
        if (query.isBlank()) return icons
        val lowerQuery = query.lowercase()
        return icons.filter { it.displayName.lowercase().contains(lowerQuery) }
    }

    private fun loadAllActionIcons(): List<ActionIcon> {
        return try {
            val actionsClass = AllIcons.Actions::class.java
            actionsClass.declaredFields
                .filter { Icon::class.java.isAssignableFrom(it.type) }
                .mapNotNull { field -> loadIconFromField(field) }
                .sortedBy { it.displayName }
        } catch (_: ReflectiveOperationException) {
            fallbackIcons()
        } catch (_: SecurityException) {
            fallbackIcons()
        }
    }

    private fun loadIconFromField(field: Field): ActionIcon? {
        return try {
            field.isAccessible = true
            val icon = field.get(null) as? Icon ?: return null
            val name = field.name
            val displayName = name
                .replace(Regex("([a-z])([A-Z])"), "$1 $2")
                .replace("_", " ")
            ActionIcon(name, displayName, icon)
        } catch (_: ReflectiveOperationException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

    private fun fallbackIcons(): List<ActionIcon> = listOf(
        ActionIcon("Suspend", "Suspend", AllIcons.Actions.Suspend),
        ActionIcon("Execute", "Execute", AllIcons.Actions.Execute),
        ActionIcon("Restart", "Restart", AllIcons.Actions.Restart)
    )
}
