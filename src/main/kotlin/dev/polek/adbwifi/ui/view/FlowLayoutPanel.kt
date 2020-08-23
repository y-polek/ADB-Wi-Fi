package dev.polek.adbwifi.ui.view

import com.intellij.ui.components.JBPanel
import java.awt.FlowLayout
import java.awt.FlowLayout.CENTER

class FlowLayoutPanel(hgap: Int = 5, vgap: Int = 5) : JBPanel<FlowLayoutPanel>(FlowLayout(CENTER, hgap, vgap))
