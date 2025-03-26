package com.jfrog.conan.clion.toolWindow

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JCEFHtmlPanel
import java.awt.Color
import javax.swing.JComponent

// AuditPanel: displays audit instructions as HTML using the same styles as ReadmePanel
class AuditPanel(val project: Project) {
    private val htmlPanel = JCEFHtmlPanel(null).apply {
        loadHTML("")
        setOpenLinksInExternalBrowser(true)
    }

    // Update the audit content with package name and version
    fun updateContent(libraryName: String, version: String) {
        val themeStyles = generateThemeStyles()
        val html = """
            <html>
            <head>
                <style>
                    $themeStyles
                    pre.code {
                        padding: 10px;
                        border-radius: 5px;
                        overflow: auto;
                        white-space: pre;
                        line-height: 1.2;
                        font-size: 13px;
                    }
                </style>
            </head>
            <body>
                <h2>🔍 Ready to secure your dependencies in seconds?</h2>
                <p>Register for free at <a href="https://audit.conan.io/register" target="_blank">audit.conan.io/register</a>.</p>
                <p>Save your token and activate it via the confirmation email you receive.</p>
                <p>Configure Conan to use your token:</p>
                <pre class="code">conan audit provider auth conancenter --token=&lt;token&gt;</pre>
                <p>Scan for vulnerabilities:</p>
                <pre class="code"># Check a specific reference
conan audit list ${'$'}{libraryName}/${'$'}{version}

# Scan the entire dependency graph
conan audit scan --requires=${'$'}{libraryName}/${'$'}{version}</pre>
                <p>Note: For more details on the Conan Audit command, please read <a href="https://example.com" target="_blank">this post</a>.</p>
                <p>Tip: To avoid exposing your token in shell history, authenticate using an environment variable (e.g., CONAN_AUDIT_PROVIDER_TOKEN_CONANCENTER=&lt;token&gt;). For more info, see the documentation.</p>
            </body>
            </html>
        """.trimIndent()
        htmlPanel.loadHTML(html)
    }

    // Returns the underlying component for UI integration
    fun getComponent(): JComponent = htmlPanel.component

    // Generate theme styles using the same values as in ReadmePanel
    private fun generateThemeStyles(): String {
        val themeScheme = LafManager.getInstance().currentUIThemeLookAndFeel
        val isDarkTheme = themeScheme.isDark
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
            }
        """.trimIndent()
    }

    // Convert a Color to a CSS-compatible rgb string
    private fun toCssColor(color: Color): String {
        return "rgb(${color.red}, ${color.green}, ${color.blue})"
    }
}
