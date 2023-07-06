package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.ide.ui.LafManager
import com.intellij.ui.Gray
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.jfrog.conan.clionplugin.models.LibraryData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Color
import javax.swing.JComponent


// FIXME: refactor to remove duplicate code from ReadmePanel
class UsedPackagesPanel {
    private val htmlPanel = JCEFHtmlPanel(null).apply {
        loadHTML("")
        setOpenLinksInExternalBrowser(true)
    }

    // The json comes from the output of https://gist.github.com/czoido/5d4ff14a700ed03e674662fd44681289
    private val resourceFile = ReadmePanel::class.java.classLoader.getResource("conan/targets-data.json")
    private val targetsData = resourceFile?.readText() ?: "{}"
    private val libraryData = Json.decodeFromString<LibraryData>(targetsData)

    private fun getScript(names: List<String>): String {
        val filteredLibraryData = LibraryData(libraries = HashMap())

        names.forEach { name ->
            val nameWithoutVersion = name.substringBefore('/')
            libraryData.libraries[nameWithoutVersion]?.let {
                // Almacenar la biblioteca utilizando el nombre completo (incluyendo la versión)
                filteredLibraryData.libraries[name] = it
            }
        }

        val filteredTargetsData = Json.encodeToString(filteredLibraryData)

        return """
        function fillExtraData() {
            const data = $filteredTargetsData;
            const libraries = data["libraries"];
            const infoDiv = document.getElementById("info");
            infoDiv.innerHTML = "";

            let cmakeFindPackageCommands = [];
            let cmakeTargetLinkLibrariesCommand = "target_link_libraries(your_exe_or_lib_name";

            let html = "<h2>Packages used by the project:</h2>";
            html += "<ul>";
            
            Object.keys(libraries).forEach(lib_name => {
                let cmake_file_name = libraries[lib_name]["cmake_file_name"] || lib_name.split('/')[0];
                let cmake_target_name = libraries[lib_name]["cmake_target_name"] || lib_name.split('/')[0] + "::" + lib_name.split('/')[0];
            
                cmakeFindPackageCommands.push("find_package(" + cmake_file_name + ")");
                cmakeTargetLinkLibrariesCommand += "\n                      " + cmake_target_name;
            
            
                html += "<li>" + lib_name;
            
                let components = ""
                if ("components" in libraries[lib_name]) {
                    for (let component in libraries[lib_name]["components"]) {
                        let cmake_target_name = libraries[lib_name]["components"][component]["cmake_target_name"] || (lib_name.split('/')[0] + "::" + component);
                        components += cmake_target_name + " ";
                    }
                }
                
                html += "<ul>";
                html += "<li>Global target: " + cmake_target_name + "</li>";
                if (components!="") {
                    html += "<li>Components targets: " + components + "</li>";
                }
            
                html += "</ul>";
                html += "</li><br>";
            });
            
            html += "</ul>";
            
            infoDiv.innerHTML = html;

            // Using libraries with CMake
            infoDiv.innerHTML += "<h2>Using libraries with CMake</h2>";
            infoDiv.innerHTML += "<p>To use the selected libraries in your own project, you can use the global targets for the packages in the CMakeLists.txt:</p>";
            infoDiv.innerHTML += "<pre class='code'># First, tell CMake to find the packages.\n# Conan will install the packages so that CMake can find them:\n\n" + cmakeFindPackageCommands.join('\n') + "\n\n# Then, link your executable or library with the corresponding package targets:\n\n" + cmakeTargetLinkLibrariesCommand + ")</pre>";
            infoDiv.innerHTML += "<p>Please, be aware that this information is generated automatically and it may contain some mistakes. If you have any problem, you can check the <a href='https://github.com/conan-io/conan-center-index/tree/master/recipes'>upstream recipes</a> to confirm the information. Also, for more detailed information on how to consume Conan packages, please <a href='https://docs.conan.io/2/tutorial/consuming_packages.html'>check the Conan documentation</a>.</p>";
        }
    """.trimIndent()
    }

    fun getHtml(names: List<String>): String {
        val themeStyles = generateThemeStyles()
        val script = getScript(names)

        return """
        <html>
        <head>
            <style>
                body {
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
        val foregroundColor = if (isDarkTheme) Gray._187 else Gray._0
        val backgroundColor = if (isDarkTheme) Color(60, 63, 65) else Gray._242
        val linkColor = if (isDarkTheme) Color(187, 134, 252) else Color(0, 0, 238)
        val blockColor = if (isDarkTheme) Gray._80 else Gray._242

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

    fun getHTMLUsedPackages(names: List<String>): JComponent {
        htmlPanel.loadHTML(getHtml(names))
        return htmlPanel.component
    }
}
