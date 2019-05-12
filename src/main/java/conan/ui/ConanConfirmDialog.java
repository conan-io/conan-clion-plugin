package conan.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConanConfirmDialog extends DialogWrapper {

    private String body;

    public ConanConfirmDialog(String title, String body) {
        super(false); // use current window as parent
        setTitle(title);
        this.body = body;
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JBLabel label = new JBLabel(body);
        label.setPreferredSize(new Dimension(100, 100));
        dialogPanel.add(label, BorderLayout.CENTER);
        return dialogPanel;
    }
}