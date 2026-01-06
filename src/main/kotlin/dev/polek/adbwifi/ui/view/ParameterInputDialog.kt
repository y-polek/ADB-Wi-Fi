package dev.polek.adbwifi.ui.view

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.ParameterPlaceholder
import javax.swing.JComponent

class ParameterInputDialog(
    private val commandName: String,
    private val placeholders: List<ParameterPlaceholder>
) : DialogWrapper(true) {

    private val inputFields: Map<String, JBTextField> = placeholders.associate { it.fullMatch to JBTextField() }

    init {
        title = PluginBundle.message("parameterInputTitle")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val builder = FormBuilder.createFormBuilder()
            .addComponent(JBLabel(PluginBundle.message("parameterInputMessage", commandName)))
            .addVerticalGap(8)

        placeholders.forEachIndexed { index, placeholder ->
            val label = when {
                placeholder.name != null -> "${placeholder.name}:"
                placeholders.size == 1 -> PluginBundle.message("parameterInputLabel")
                else -> PluginBundle.message("parameterInputLabelIndexed", index + 1)
            }
            builder.addLabeledComponent(label, inputFields[placeholder.fullMatch]!!)
        }

        return builder.panel
    }

    fun getParameterValues(): Map<String, String> =
        inputFields.mapValues { it.value.text }
}
