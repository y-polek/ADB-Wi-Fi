package dev.polek.adbwifi.model

import com.intellij.icons.AllIcons
import javax.swing.Icon

enum class CommandIcon(val id: String, val displayName: String, val icon: Icon) {
    SUSPEND("suspend", "Stop", AllIcons.Actions.Suspend),
    EXECUTE("execute", "Play", AllIcons.Actions.Execute),
    RESTART("restart", "Restart", AllIcons.Actions.Restart),
    CLEAR("clear", "Clear", AllIcons.Actions.ClearCash),
    FORCE_REFRESH("forceRefresh", "Force Refresh", AllIcons.Actions.ForceRefresh),
    LIGHTNING("lightning", "Lightning", AllIcons.Actions.Lightning),
    CONSOLE("console", "Console", AllIcons.Debugger.Console),
    DUMP("dump", "Dump", AllIcons.Actions.Dump),
    GC("gc", "Garbage Collect", AllIcons.Actions.GC),
    DOWNLOAD("download", "Download", AllIcons.Actions.Download),
    UPLOAD("upload", "Upload", AllIcons.Actions.Upload),
    EDIT("edit", "Edit", AllIcons.Actions.Edit),
    DELETE("delete", "Delete", AllIcons.Actions.DeleteTag),
    SEARCH("search", "Search", AllIcons.Actions.Search),
    ;

    companion object {
        fun fromId(id: String): CommandIcon? = entries.find { it.id == id }
        fun default(): CommandIcon = CONSOLE
    }
}
