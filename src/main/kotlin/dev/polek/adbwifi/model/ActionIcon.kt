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
        ActionIcon("restart", "Restart 1", AllIcons.Actions.Restart),
        ActionIcon("restart-2", "Restart 2", AllIcons.Actions.RestartStop),
        ActionIcon("restart-3", "Restart 3", AllIcons.Actions.RestartFrame),
        ActionIcon("pause", "Pause", AllIcons.Actions.Pause),

        // Data & Storage
        ActionIcon("clear", "Clear 1", AllIcons.Actions.GC),
        ActionIcon("clear-2", "Clear 2", AllIcons.Actions.ClearCash),
        ActionIcon("delete", "Delete 1", AllIcons.General.Delete),
        ActionIcon("delete-2", "Delete 2", AllIcons.Actions.DeleteTag),
        ActionIcon("delete-3", "Delete 3", AllIcons.Actions.DeleteTagHover),
        ActionIcon("trash", "Trash", AllIcons.Actions.Cancel),

        // Refresh & Sync
        ActionIcon("refresh", "Refresh 1", AllIcons.Actions.Refresh),
        ActionIcon("refresh-2", "Refresh 2", AllIcons.Actions.ForceRefresh),
        ActionIcon("refresh-3", "Refresh 3", AllIcons.Actions.StopRefresh),

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
        ActionIcon("help", "Help", AllIcons.Actions.Help),
        ActionIcon("warning", "Warning", AllIcons.General.Warning),
        ActionIcon("error", "Error", AllIcons.General.Error),

        // Actions
        ActionIcon("lightning", "Lightning", AllIcons.Actions.Lightning),
        ActionIcon("console", "Console", AllIcons.Debugger.Console),

        // Navigation
        ActionIcon("back", "Back", AllIcons.Actions.Back),
        ActionIcon("forward", "Forward", AllIcons.Actions.Forward),
        ActionIcon("up", "Up", AllIcons.Actions.MoveUp),
        ActionIcon("down", "Down", AllIcons.Actions.MoveDown),
        ActionIcon("home", "Home", AllIcons.Nodes.HomeFolder),

        // Settings & Config
        ActionIcon("settings", "Settings", AllIcons.General.Settings),

        // Misc
        ActionIcon("pin", "Pin", AllIcons.General.Pin_tab),
        ActionIcon("star", "Star", AllIcons.Nodes.Favorite),
        ActionIcon("flag", "Flag", AllIcons.Nodes.NotFavoriteOnHover),
        ActionIcon("link", "Link 1", AllIcons.Ide.Link),
        ActionIcon("external", "Link 2", AllIcons.Ide.External_link_arrow),
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

    fun getIconById(id: String): ActionIcon? =
        icons.find { it.id.equals(id, ignoreCase = true) }

    fun search(query: String): List<ActionIcon> {
        if (query.isBlank()) return icons
        val lowerQuery = query.lowercase()
        return icons.filter {
            it.displayName.lowercase().contains(lowerQuery) ||
                it.id.lowercase().contains(lowerQuery)
        }
    }
}
