package com.jfrog.conan.clionplugin.models

import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class ListResult : TableModel {
    data class ListResultRow(val name: String, val latestVersion: String, val description: String)
    private val data: MutableList<ListResultRow> = mutableListOf()
    override fun getRowCount(): Int {
        return data.size
    }

    override fun getColumnCount(): Int {
        return 2
    }

    override fun getColumnName(columnIndex: Int): String {
        return listOf("Name", "Latest Version")[columnIndex]
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return String::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): String {
        return if (columnIndex == 0) data[rowIndex].name else data[rowIndex].latestVersion
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {

    }

    override fun addTableModelListener(l: TableModelListener?) {

    }

    override fun removeTableModelListener(l: TableModelListener?) {

    }

    fun updateList(newList: List<ListResultRow> ) {
        data.clear()
        data.addAll(newList)
    }

    fun getRecipeAtRow(rowIndex: Int): ListResultRow {
        return data[rowIndex]
    }
}
