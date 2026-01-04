package dev.polek.adbwifi.model

import com.intellij.icons.AllIcons
import javax.swing.Icon

data class ActionIcon(
    val id: String,
    val displayName: String,
    val icon: Icon
)

object ActionIconsProvider {

    private val icons: List<ActionIcon> = listOf(
        // App lifecycle
        ActionIcon("play", "Play", AllIcons.Actions.Execute),
        ActionIcon("stop", "Stop", AllIcons.Actions.Suspend),
        ActionIcon("restart", "Restart", AllIcons.Actions.Restart),
        ActionIcon("pause", "Pause", AllIcons.Actions.Pause),

        // Data & Storage
        ActionIcon("clear", "Clear", AllIcons.Actions.GC),
        ActionIcon("delete", "Delete", AllIcons.Actions.DeleteTag),
        ActionIcon("trash", "Trash", AllIcons.Actions.Cancel),

        // Refresh & Sync
        ActionIcon("refresh", "Refresh", AllIcons.Actions.Refresh),
        ActionIcon("force-refresh", "Force Refresh", AllIcons.Actions.ForceRefresh),
        ActionIcon("sync", "Sync", AllIcons.Actions.Refresh),

        // Transfer
        ActionIcon("download", "Download", AllIcons.Actions.Download),
        ActionIcon("upload", "Upload", AllIcons.Actions.Upload),

        // Search & Find
        ActionIcon("search", "Search", AllIcons.Actions.Search),
        ActionIcon("find", "Find", AllIcons.Actions.Find),

        // Edit & Modify
        ActionIcon("edit", "Edit", AllIcons.Actions.Edit),
        ActionIcon("copy", "Copy", AllIcons.Actions.Copy),

        // Status & Info
        ActionIcon("info", "Info", AllIcons.Actions.Help),
        ActionIcon("warning", "Warning", AllIcons.General.Warning),
        ActionIcon("error", "Error", AllIcons.General.Error),

        // Actions
        ActionIcon("lightning", "Lightning", AllIcons.Actions.Lightning),
        ActionIcon("console", "Console", AllIcons.Debugger.Console),
        ActionIcon("terminal", "Terminal", AllIcons.Debugger.Console),

        // Navigation
        ActionIcon("back", "Back", AllIcons.Actions.Back),
        ActionIcon("forward", "Forward", AllIcons.Actions.Forward),
        ActionIcon("up", "Up", AllIcons.Actions.MoveUp),
        ActionIcon("down", "Down", AllIcons.Actions.MoveDown),
        ActionIcon("home", "Home", AllIcons.Nodes.HomeFolder),

        // Settings & Config
        ActionIcon("settings", "Settings", AllIcons.General.Settings),
        ActionIcon("configure", "Configure", AllIcons.General.GearPlain),

        // Misc
        ActionIcon("pin", "Pin", AllIcons.General.Pin_tab),
        ActionIcon("favorite", "Favorite", AllIcons.Nodes.Favorite),
        ActionIcon("star", "Star", AllIcons.Nodes.Favorite),
        ActionIcon("flag", "Flag", AllIcons.Nodes.NotFavoriteOnHover),
        ActionIcon("link", "Link", AllIcons.Ide.Link),
        ActionIcon("external", "External Link", AllIcons.Ide.External_link_arrow),
        ActionIcon("eye", "Show", AllIcons.Actions.Show),
        ActionIcon("hide", "Hide", AllIcons.Actions.ToggleVisibility),
        ActionIcon("lock", "Lock", AllIcons.Diff.Lock),
        ActionIcon("filter", "Filter", AllIcons.General.Filter),
        ActionIcon("expand", "Expand", AllIcons.Actions.Expandall),
        ActionIcon("collapse", "Collapse", AllIcons.Actions.Collapseall),
        ActionIcon("add", "Add", AllIcons.General.Add),
        ActionIcon("remove", "Remove", AllIcons.General.Remove),
        ActionIcon("check", "Check", AllIcons.Actions.Checked),
        ActionIcon("close", "Close", AllIcons.Actions.Close),
        ActionIcon("more", "More", AllIcons.Actions.More),
    ).sortedBy { it.displayName }

    fun getAllIcons(): List<ActionIcon> = icons

    fun getIconById(id: String): ActionIcon? =
        icons.find { it.id.equals(id, ignoreCase = true) }

    fun getDefaultIcon(): ActionIcon = icons.find { it.id == "play" } ?: icons.first()

    fun search(query: String): List<ActionIcon> {
        if (query.isBlank()) return icons
        val lowerQuery = query.lowercase()
        return icons.filter {
            it.displayName.lowercase().contains(lowerQuery) ||
                it.id.lowercase().contains(lowerQuery)
        }
    }
}
