package dev.polek.adbwifi.model

sealed interface CommandListItem {

    data class AdbCommandGroup(
        val name: String,
        val iconId: String = "",
        val iconColor: String = "primary",
        val commands: List<AdbCommand> = emptyList()
    ) : CommandListItem

    data class AdbCommand(
        val name: String,
        val command: String,
        val iconId: String = "",
        val iconColor: String = "primary",
        val isEnabled: Boolean,
        val requiresConfirmation: Boolean = false
    ) : CommandListItem {
        val requiresPackage: Boolean get() = command.contains("{package}")

        val parameterPlaceholders: List<ParameterPlaceholder>
            get() {
                val filePlaceholders = FILE_REGEX.findAll(command).map { match ->
                    ParameterPlaceholder(
                        fullMatch = match.value,
                        type = PlaceholderType.FILE,
                        extension = match.groupValues[1].trim().takeIf { it.isNotEmpty() },
                        name = match.groupValues[2].trim().takeIf { it.isNotEmpty() }
                    )
                }
                val otherPlaceholders = PARAM_DIR_REGEX.findAll(command).map { match ->
                    ParameterPlaceholder(
                        fullMatch = match.value,
                        type = PlaceholderType.fromString(match.groupValues[1]),
                        name = match.groupValues[2].trim().takeIf { it.isNotEmpty() }
                    )
                }
                return (filePlaceholders + otherPlaceholders).distinctBy { it.fullMatch }.toList()
            }

        val requiresParameters: Boolean get() = parameterPlaceholders.isNotEmpty()

        private companion object {
            private val FILE_REGEX = """\{file(\s+\.[a-zA-Z0-9]+)?(\s+[^}]+)?}""".toRegex()
            private val PARAM_DIR_REGEX = """\{(param|dir)(\s+[^}]+)?}""".toRegex()
        }
    }
}

enum class PlaceholderType {
    PARAM,
    FILE,
    DIR;

    companion object {
        fun fromString(value: String): PlaceholderType = when (value) {
            "file" -> FILE
            "dir" -> DIR
            else -> PARAM
        }
    }
}

data class ParameterPlaceholder(
    val fullMatch: String,
    val type: PlaceholderType = PlaceholderType.PARAM,
    val extension: String? = null,
    val name: String?
)
