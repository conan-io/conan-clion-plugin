package com.jfrog.conan.clion.toolWindow

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.jfrog.conan.clion.services.ConanService
import java.awt.Color
import javax.swing.JComponent
import javax.swing.SwingUtilities


class ReadmePanel(val project: Project) {
    private val htmlPanel = JCEFHtmlPanel(null).apply {
        loadHTML("")
        setOpenLinksInExternalBrowser(true)
    }.also { htmlPanel ->
        val cefBrowser = htmlPanel.cefBrowser
        cefBrowser.uiComponent.addMouseWheelListener { e ->
            val scrollPane = SwingUtilities.getAncestorOfClass(JBScrollPane::class.java, e.component) as? JBScrollPane
            scrollPane?.dispatchEvent(SwingUtilities.convertMouseEvent(e.component, e, scrollPane))
        }
    }

    private val targetsData = project.service<ConanService>().getRemoteDataText()
    private val libraryData = project.service<ConanService>().getRemoteData()

    fun getTitleHtml(name: String): String {
        val description = libraryData.libraries[name]?.description
        val licenses = libraryData.libraries[name]?.license
        var html =  "<html>" +
                "&nbsp;<strong><font size='11'>$name</font></strong><br>" +
                "&nbsp;<font size='5'>$description</font>"
        if (licenses!=null) {
            html += "<br>&nbsp;<font size='6'>&#x2696;</font>&nbsp;<strong><font size='4'>${licenses.joinToString(", ")}</font></strong>"
        }
        html += "<br>"
        return html
    }


    private fun getScript(name: String): String {
        return """
            function fillExtraData() {
                const data = $targetsData;
                const libraries = data["libraries"]
                const infoDiv = document.getElementById("info");
                const selected_lib = "$name";
                infoDiv.innerHTML = "";

                let cmake_file_name = libraries[selected_lib]["cmake_file_name"] || selected_lib;
                let cmake_target_name = libraries[selected_lib]["cmake_target_name"] || selected_lib + "::" + selected_lib;

                if (libraries[selected_lib]["v2"] == false) {
                    let warning = "<div class='warning'><span>&#x26A0;</span> Warning: This library is not compatible with Conan v2. If you need to use this library, please <a href='https://github.com/conan-io/conan-center-index/issues/new?title=Library " + selected_lib + " is not compatible with Conan v2&body=This issue comes from the Clion plugin'>open an issue in Conan Center Index</a>.</div>";
                    infoDiv.innerHTML += warning;
                }

                // Using $name with CMake
                infoDiv.innerHTML += "<h2>Using " + selected_lib + " with CMake</h2>";
                infoDiv.innerHTML += "<p>To use <strong>" + selected_lib + "</strong> in your own project, you can use the global target for the package in the CMakeLists.txt:";

                infoDiv.innerHTML += "<pre class='code'># First, tell CMake to find the package.\n# Conan will install the packages so that CMake can find it:\n\nfind_package(" + cmake_file_name + ")\n\n# Then, link your executable or library with the corresponding package targets:\n\ntarget_link_libraries(your_exe_or_lib_name " + cmake_target_name + ")</pre>";

                infoDiv.innerHTML += "Please, be aware that this information is generated automatically and it may contain some mistakes. If you have any problem, you can check the <a href='https://github.com/conan-io/conan-center-index/tree/master/recipes/" + selected_lib + "'>upstream recipe</a> to confirm the information. Also, for more detailed information on how to consume Conan packages, please <a href='https://docs.conan.io/2/tutorial/consuming_packages.html'>check the Conan documentation</a>.</p>";

                // Components
                if ("components" in libraries[selected_lib]) {
                    infoDiv.innerHTML += "<h2>Declared components for " + selected_lib + "</h2>";
                    infoDiv.innerHTML += "<p>This library declares components, so you can use the components targets in your project instead of the global target. There are the declared CMake target names for the library's components:<br>";
                    infoDiv.innerHTML += "<ul>";
                    for (let component in libraries[selected_lib]["components"]) {
                        let cmake_target_name = libraries[selected_lib]["components"][component]["cmake_target_name"] || (selected_lib + "::" + component);
                        infoDiv.innerHTML += "<li>" + component + ": <code>" + cmake_target_name + "</code></li>";
                    }
                    infoDiv.innerHTML += "</ul></p>";
                }
            }
        """.trimIndent()
    }

    fun getHtml(name: String): String {
        val themeStyles = generateThemeStyles()
        val script = getScript(name)

        return """
        <html>
        <head>
            <style>
                ::-webkit-scrollbar {
                    display: none;
                }
                body {
                    overflow: -moz-scrollbars-none;
                    -ms-overflow-style: none;  /* IE and Edge */
                }
                html, body {
                    overflow: auto;
                }
                $themeStyles
            </style>
            <script>
                $script
            </script>
        </head>
        <body onload="fillExtraData()">
            <div id="info"></div>
        </body>
        </html>
    """.trimIndent()
    }

    private fun generateThemeStyles(): String {
        val themeScheme = LafManager.getInstance().currentLookAndFeel

        val lafClassName = themeScheme.className ?: "com.intellij.ide.ui.laf.intellij.IntelliJLookAndFeel"
        // TODO: make more advanced theme detection?
        val isDarkTheme = lafClassName.contains("Darcula", ignoreCase = true)
        val foregroundColor = if (isDarkTheme) Color(187, 187, 187) else Color(0, 0, 0)
        val backgroundColor = if (isDarkTheme) Color(60, 63, 65) else Color(242, 242, 242)
        val linkColor = if (isDarkTheme) Color(187, 134, 252) else Color(0, 0, 238)
        val blockColor = if (isDarkTheme) Color(80, 80, 80) else Color(242, 242, 242)

        return """
            body {
                color: ${toCssColor(foregroundColor)};
                background-color: ${toCssColor(backgroundColor)};
                font-family: sans-serif;
            }
            a {
                color: ${toCssColor(linkColor)};
                text-decoration: none;
            }
            a:hover {
                text-decoration: underline;
            }
            .code {
                background-color: ${toCssColor(blockColor)};
                padding: 10px;
                border-radius: 5px;
                overflow: auto;
                white-space: pre;
                line-height: 1.2;
                font-size: 13px;
            }
            .warning {
                background-color: ${toCssColor(blockColor)};
                padding: 10px;
                border-radius: 5px;
                overflow: auto;
                line-height: 1.2;
                font-size: 14px;
            }
            """.trimIndent()
    }

    private fun toCssColor(color: Color): String {
        return "rgb(${color.red}, ${color.green}, ${color.blue})"
    }

    fun getHTMLPackageInfo(name: String): JComponent {
        htmlPanel.loadHTML(getHtml(name))
        return htmlPanel.component
    }
}
