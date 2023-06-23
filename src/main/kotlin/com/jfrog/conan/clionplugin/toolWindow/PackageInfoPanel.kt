package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.ui.jcef.JCEFHtmlPanel
import javax.swing.JComponent

class PackageInfoPanel {

    private val htmlPanel = JCEFHtmlPanel(null).apply {
        //alignmentX = Component.LEFT_ALIGNMENT
        loadHTML("")
    }

    private val resourceFile = PackageInfoPanel::class.java.classLoader.getResource("conan/targets-data.json")
    private val targetsData = resourceFile?.readText() ?: "{}"

    fun getScript(name: String): String {
        return """
                function fillExtraData() {
                    const data = $targetsData
                    const cpp_info = document.getElementById("cpp_info");
                    cpp_info.innerText = ""
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
        return """
                    <html>
                    <head>
                        <style>
                            body {
                                color: rgb(187, 187, 187);
                                font-family: sans-serif;
                            }
                        </style>
                        <script>
                            ${getScript(name)}
                        </script>
                    </head>
                    <body onload="fillExtraData()">
                        <div id="cpp_info"></div>
                    </body>
                    </html>
                """.trimIndent()
    }


    fun getComponent(name: String): JComponent {
        htmlPanel.loadHTML(getHtml(name))
        return htmlPanel.component
    }
}