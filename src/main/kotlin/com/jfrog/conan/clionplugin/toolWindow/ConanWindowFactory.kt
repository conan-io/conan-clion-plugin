package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBSplitter
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.text.SemVer
import com.intellij.util.ui.JBUI
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.conan.datamodels.Recipe
import com.jfrog.conan.clionplugin.models.LibrariesTableModel
import com.jfrog.conan.clionplugin.services.ConanService
import com.jfrog.conan.clionplugin.services.RemotesDataStateService
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter


class ConanWindowFactory : ToolWindowFactory {

    private val contentFactory = ContentFactory.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = ConanWindow(toolWindow, project)
        val content = contentFactory.createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class ConanWindow(toolWindow: ToolWindow, val project: Project) {
        private val stateService = project.service<RemotesDataStateService>()
        private val conanService = project.service<ConanService>()
        private val libraryPanel = PackageInformationPanel(conanService, ReadmePanel())

        fun getContent() = OnePixelSplitter(false).apply {

            val secondComponentPanel = JBPanelWithEmptyText()

            secondComponentPanel.layout = BoxLayout(secondComponentPanel, BoxLayout.Y_AXIS)

            firstComponent = DialogPanel(BorderLayout()).apply {
                border = JBUI.Borders.empty(5)
                val searchTextField = SearchTextField()
                val mainActionToolbar = MainActionToolbar(project).getContent()
                mainActionToolbar.targetComponent = this

                var recipes: List<Recipe> = listOf()
                val dataModel = LibrariesTableModel(0)

                stateService.addStateChangeListener(object : RemotesDataStateService.RemoteDataStateListener {
                    override fun stateChanged(newState: RemotesDataStateService.State?) {
                        dataModel.rowCount = 0
                        recipes = listOf()

                        if (newState == null) return

                        // conancenter has one entry per recipe version, this collates all versions into 1 recipe object,
                        // with a versions list of each of the existing ones
                        recipes = newState.conancenter.keys
                            .map {
                                val split = it.split("/")
                                Pair(split[0], split[1])
                            }
                            .groupBy { it.first }
                            .map {
                                dataModel.addRow(arrayOf(it.key))
                                Recipe(it.key, it.value.map { it.second })
                            }
                    }
                })

                val packagesTable = JBTable(dataModel).apply {
                    tableHeader
                    autoCreateRowSorter = true
                    (rowSorter as TableRowSorter<DefaultTableModel>).sortKeys =
                        mutableListOf(RowSorter.SortKey(0, SortOrder.ASCENDING))
                }


                searchTextField.apply {
                    addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            (packagesTable.rowSorter as TableRowSorter<DefaultTableModel>).rowFilter =
                                RowFilter.regexFilter(".*$text.*")
                        }
                    })
                }

                packagesTable.selectionModel.addListSelectionListener {
                    val selectedRow = packagesTable.selectedRow
                    if (selectedRow != -1) {
                        val name = packagesTable.getValueAt(selectedRow, 0) as String

                        val recipe = recipes.find { it.name == name } ?: throw Exception()
                        val versions = recipe.versions.sortedByDescending(SemVer::parseFromText)

                        libraryPanel.updatePanel(name, versions)
                    }
                }

                add(JBSplitter().apply {
                    firstComponent = searchTextField
                    secondComponent = JPanel(BorderLayout()).apply {
                        add(mainActionToolbar.component, BorderLayout.EAST)
                    }
                }, BorderLayout.PAGE_START)
                add(JBScrollPane(packagesTable), BorderLayout.CENTER)
            }
            secondComponent = libraryPanel.apply { withEmptyText(UIBundle.message("library.description.empty")) }
            proportion = 0.2f
        }
    }
}
