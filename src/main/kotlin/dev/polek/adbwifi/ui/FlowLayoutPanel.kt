package dev.polek.adbwifi.ui

import com.intellij.ui.components.JBPanel
import java.awt.FlowLayout

class FlowLayoutPanel(hgap: Int = 5, vgap: Int = 5) : JBPanel<FlowLayoutPanel>(FlowLayout(FlowLayout.CENTER, hgap, vgap))
