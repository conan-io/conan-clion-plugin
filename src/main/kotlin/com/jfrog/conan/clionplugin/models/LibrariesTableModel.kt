package com.jfrog.conan.clionplugin.models

import com.jfrog.conan.clionplugin.bundles.UIBundle
import javax.swing.table.DefaultTableModel

class LibrariesTableModel(rowCount: Int) :
    DefaultTableModel(arrayOf(UIBundle.message("libraries.list.table.name")), rowCount) {
    // By default cells are editable and that's no good. Override the function that tells the UI it is
    // TODO: Find the proper configuration for this, this can't be the proper way to make it static
    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }
}
