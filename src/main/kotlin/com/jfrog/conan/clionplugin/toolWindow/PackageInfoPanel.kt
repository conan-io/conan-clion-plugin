package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.ide.ui.LafManager
import com.intellij.ui.jcef.JCEFHtmlPanel
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.awt.Color
import javax.swing.JComponent


@Serializable
data class Library(
        val description: String,
        val license: List<String>,
        val v2: Boolean,
        val cmake_file_name: String? = null,
        val cmake_target_name: String? = null,
        val components: HashMap<String, Component>? = null
)

@Serializable
data class Component(
        val cmake_target_name: String? = null
)

@Serializable
data class LibraryData(
        val libraries: HashMap<String, Library>
)

class PackageInfoPanel {
    private val htmlPanel = JCEFHtmlPanel(null).apply {
        loadHTML("")
    }

    // The json comes from the output of https://gist.github.com/czoido/5d4ff14a700ed03e674662fd44681289
    private val resourceFile = PackageInfoPanel::class.java.classLoader.getResource("conan/targets-data.json")
    private val targetsData = resourceFile?.readText() ?: "{}"
    private val libraryData = Json.decodeFromString<LibraryData>(targetsData)

    fun getTitleHtml(name: String): String {
        val description = libraryData.libraries[name]?.description
        return "<html>&nbsp;<strong><font size='11'>$name</font></strong><br>&nbsp;<font size='4'>$description</font><br><br>"
    }

    private fun getScript(name: String): String {
        return """
            function fillExtraData() {
                const data = $targetsData;
                const cpp_info = document.getElementById("cpp_info");
                cpp_info.innerText = "";
                if ("$name" in data) {
                    if ("cmake_file_name" in data["$name"]) {
                        cpp_info.innerText += "cmake_file_name: " + data["$name"]["cmake_file_name"] + "\n";
                    }
                    if ("cmake_target_name" in data["$name"]) {
                        cpp_info.innerText += "cmake_target_name: " + data["$name"]["cmake_target_name"] + "\n";
                    }
                    if ("components" in data["$name"]) {
                        cpp_info.innerText += "components: ";
                        for (let component in data["$name"]["components"]) {
                            cpp_info.innerText += data["$name"]["components"][component]["cmake_target_name"] + "\n";
                        }
                    }
                }
                else {
                    cpp_info.innerText += "cmake_file_name: $name\n";
                    cpp_info.innerText += "cmake_target_name: $name::$name\n";
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
                $themeStyles
            </style>
            <script>
                $script
            </script>
        </head>
        <body onload="fillExtraData()">
            <div id="cpp_info"></div>
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

        return """
        body {
            color: ${toCssColor(foregroundColor)};
            background-color: ${toCssColor(backgroundColor)};
            font-family: sans-serif;
        }
    """.trimIndent()
    }

    private fun toCssColor(color: Color): String {
        return "rgb(${color.red}, ${color.green}, ${color.blue})"
    }

    fun getComponent(name: String): JComponent {
        htmlPanel.loadHTML(getHtml(name))
        return htmlPanel.component
    }
}
