package dev.polek.adbwifi.model

data class AdbCommandConfig(
    val name: String,
    val command: String,
    val iconId: String = "",
    val isEnabled: Boolean,
    val order: Int,
    val requiresConfirmation: Boolean = false
) {
    val requiresPackage: Boolean get() = command.contains("{package}")

    val parameterPlaceholders: List<ParameterPlaceholder>
        get() = PARAMETER_REGEX.findAll(command).map { match ->
            ParameterPlaceholder(
                fullMatch = match.value,
                name = match.groupValues[2].trim().takeIf { it.isNotEmpty() }
            )
        }.distinctBy { it.fullMatch }.toList()

    val requiresParameters: Boolean get() = parameterPlaceholders.isNotEmpty()

    private companion object {
        private val PARAMETER_REGEX = """\{(param\d*)(\s+[^}]+)?}""".toRegex()
    }
}

data class ParameterPlaceholder(
    val fullMatch: String,
    val name: String?
)
