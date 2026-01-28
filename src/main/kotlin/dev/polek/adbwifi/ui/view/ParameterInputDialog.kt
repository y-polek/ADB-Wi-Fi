package dev.polek.adbwifi.ui.view

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.ParameterPlaceholder
import dev.polek.adbwifi.model.PlaceholderType
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.text.JTextComponent

class ParameterInputDialog(
    private val commandName: String,
    private val placeholders: List<ParameterPlaceholder>
) : DialogWrapper(true) {

    private val inputFields: Map<String, JTextComponent> = placeholders.associate { placeholder ->
        placeholder.fullMatch to when (placeholder.type) {
            PlaceholderType.FILE -> createBrowseField(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle(PluginBundle.message("parameterInputSelectFile"))
            )
            PlaceholderType.DIR -> createBrowseField(
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle(PluginBundle.message("parameterInputSelectDirectory"))
            )
            PlaceholderType.PARAM -> JBTextField()
        }
    }

    private fun createBrowseField(descriptor: FileChooserDescriptor): JTextComponent {
        return TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(null, descriptor)
        }.textField
    }

    init {
        title = PluginBundle.message("parameterInputTitle")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val builder = FormBuilder.createFormBuilder()
            .addComponent(JBLabel(PluginBundle.message("parameterInputMessage", commandName)))
            .addVerticalGap(8)

        placeholders.forEach { placeholder ->
            val label = placeholder.name?.let { "$it:" }
                ?: PluginBundle.message(placeholder.type.defaultLabelKey)
            val field = inputFields[placeholder.fullMatch]!!
            val component = field.parent as? TextFieldWithBrowseButton ?: field
            builder.addLabeledComponent(label, component)
        }

        return builder.panel.apply {
            minimumSize = Dimension(JBUI.scale(400), 0)
        }
    }

    fun getParameterValues(): Map<String, String> =
        inputFields.mapValues { it.value.text }
}

private val PlaceholderType.defaultLabelKey: String
    get() = when (this) {
        PlaceholderType.PARAM -> "parameterInputLabel"
        PlaceholderType.FILE -> "parameterInputFileLabel"
        PlaceholderType.DIR -> "parameterInputDirLabel"
    }
