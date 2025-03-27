package com.jfrog.conan.clion.toolWindow

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.jcef.JCEFHtmlPanel
import java.awt.Color
import javax.swing.JComponent
import javax.swing.SwingUtilities

class AuditPanel(val project: Project) {
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
                        white-space: pre-wrap;
                        line-height: 1.2;
                        font-size: 13px;
                    }
                </style>
            </head>
            <body>
                <div style="margin: 10px;">
                    <h2>Secure your dependencies using <code>conan audit</code></h2>
                    <p>
                        <strong>Register</strong> for free at 
                        <a href="https://audit.conan.io/register">audit.conan.io/register</a>.
                    </p>
                    <p>
                        <strong>Save your token</strong> and <strong>activate it</strong> via the confirmation email you receive.
                    </p>
                    <p>
                        <strong>Configure Conan to use your token</strong>:
                    </p>
                    <pre class="code">conan audit provider auth conancenter --token=&lt;token&gt;</pre>
                    <p>
                        <strong>Scan for vulnerabilities</strong>:
                    </p>
                    <pre class="code">
# Check a specific reference
conan audit list ${libraryName}/${version}

# Scan the entire dependency graph
conan audit scan --requires=${libraryName}/${version}
                    </pre>
                    <p>
                        <strong>Note:</strong> For more details on the Conan Audit command, please read 
                        <a href="https://blog.conan.io/introducing-conan-audit-command/">this post</a>.
                    </p>
                    <p>
                        <strong>Tip:</strong> To avoid exposing your token in shell history, authenticate using an environment variable 
                        (e.g., <code>CONAN_AUDIT_PROVIDER_TOKEN_CONANCENTER=&lt;token&gt;</code>). For more info, see <a href="https://docs.conan.io/2/devops/audit.html">the documentation</a>.
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()
        htmlPanel.loadHTML(html)
    }

    // Returns the underlying component for UI integration
    fun getComponent(): JComponent = htmlPanel.component

    private fun generateThemeStyles(): String {
        val themeScheme = LafManager.getInstance().currentUIThemeLookAndFeel

        val isDarkTheme = themeScheme.isDark
        val foregroundColor = if (isDarkTheme) Color(187, 187, 187) else Color(0, 0, 0)
        val backgroundColor = if (isDarkTheme) Color(60, 63, 65) else Color(242, 242, 242)
        val linkColor = if (isDarkTheme) Color(187, 134, 252) else Color(0, 0, 238)
        val blockColor = if (isDarkTheme) Color(80, 80, 80) else Color(212, 212, 212)

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
        """.trimIndent()
    }

    private fun toCssColor(color: Color): String {
        return "rgb(${color.red}, ${color.green}, ${color.blue})"
    }
}
