package dev.polek.adbwifi.utils

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object Icons {
    val USB = IconLoader.getIcon("/icons/usbIcon.svg", Icons::class.java)
    val NO_USB = IconLoader.getIcon("/icons/noUsbIcon.svg", Icons::class.java)
    val WIFI = IconLoader.getIcon("/icons/wifiIcon.svg", Icons::class.java)
    val NO_WIFI = IconLoader.getIcon("/icons/noWifi.svg", Icons::class.java)
    val WIFI_NETWORK = IconLoader.getIcon("/icons/wifiNetwork.svg", Icons::class.java)
    val MOBILE_NETWORK = IconLoader.getIcon("/icons/mobileNetwork.svg", Icons::class.java)
    val HOTSPOT_NETWORK = IconLoader.getIcon("/icons/hotspotNetwork.svg", Icons::class.java)
    val DEVICE_LINEUP = IconLoader.getIcon("/icons/devices-lineup.png", Icons::class.java)
    val DEVICE_WARNING = IconLoader.getIcon("/icons/deviceWarning.png", Icons::class.java)
    val MENU = IconLoader.getIcon("/icons/menuIcon.svg", Icons::class.java)
    val SHARE_SCREEN = IconLoader.getIcon("/icons/shareScreen.svg", Icons::class.java)
    val DELETE = IconLoader.getIcon("/icons/deleteIcon.svg", Icons::class.java)
    val OK = IconLoader.getIcon("AllIcons.General.InspectionsOK", Icons::class.java)
    val ERROR = IconLoader.getIcon("AllIcons.General.Error", Icons::class.java)

    // Device type icons
    val PHONE = IconLoader.getIcon("/icons/phoneIcon.svg", Icons::class.java)

    // UI icons
    val PLUS = IconLoader.getIcon("/icons/plusIcon.svg", Icons::class.java)
    val PLUS_WHITE = IconLoader.getIcon("/icons/plusIconWhite.svg", Icons::class.java)
    val HELP = IconLoader.getIcon("/icons/helpIcon.svg", Icons::class.java)

    val ADB_COMMANDS: Icon = AllIcons.Actions.Execute
}
