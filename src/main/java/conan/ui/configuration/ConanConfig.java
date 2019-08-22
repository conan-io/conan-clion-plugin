package conan.ui.configuration;

import com.intellij.execution.Output;
import com.intellij.execution.OutputListener;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import conan.commands.ConfigInstall;
import conan.persistency.PersistencyUtils;
import conan.persistency.settings.ConanProjectSettings;
import conan.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static conan.persistency.Keys.CONFIG_CONAN_EXE_PATH;
import static conan.persistency.Keys.CONFIG_INSTALL_LOCATION;

/**
 * Represents the Conan settings form.
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConanConfig implements Configurable, Configurable.NoScroll {

    public static final String CONFIG_NAME = "Conan";
    private Project project;
    private JPanel rootPanel;

    private JBTextField configInstallLocation;
    private JButton installConfigButton;
    private JLabel configInstallRes;
    private JBTextField installArgs;
    private JLabel installArgsLabel;

    private JLabel conanPathLabel;
    private TextFieldWithBrowseButton conanPath;

    public ConanConfig(@NotNull Project project) {
        this.project = project;
        if (project.isDefault()) { // No project
            installArgs.setVisible(false);
            installArgsLabel.setVisible(false);
            conanPathLabel.setVisible(false);
            conanPath.setVisible(false);
        }
    }

    @Nls
    @Override
    public String getDisplayName() {
        return CONFIG_NAME;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        installConfigButton.addActionListener(actionEvent -> {
            String location = configInstallLocation.getText();
            if (StringUtils.isBlank(location)) {
                location = PersistencyUtils.getValue(CONFIG_INSTALL_LOCATION);
            }
            PersistencyUtils.setValue(CONFIG_INSTALL_LOCATION, location);

            if (!(Utils.validateUrl(location) || new File(location).exists())) {
                setConfigInstallRes("Invalid URL or path", false);
                return;
            }
            runConfigInstall(location);
        });
        configInstallLocation.getEmptyText().setText("Git repository, local folder or ZIP file (local or http)");
        installArgs.getEmptyText().setText("Arguments other than '--if', '--pr' and '--update'");
        String envExePath = System.getenv("CONAN_EXE_PATH");
        if( envExePath != null) {
            conanPath.setText(envExePath);
        }
        conanPath.setToolTipText("Path to the Conan executable, by default it will search in the path");

        conanPath.addActionListener(actionEvent -> {
            final FileChooserDescriptor d = FileChooserDescriptorFactory.createSingleFileDescriptor();
            VirtualFile initialFile = StringUtil.isNotEmpty(conanPath.getText()) ? LocalFileSystem.getInstance().findFileByPath(conanPath.getText()) : null;
            VirtualFile file = FileChooser.chooseFile(d, project, initialFile);
            if (file != null) {
                conanPath.setText(file.getCanonicalPath());
            }
        });

        return rootPanel;
    }

    /**
     * Run {@link ConfigInstall}.
     *
     * @param location the location of the Conan configuration.
     */
    private void runConfigInstall(String location) {
        OutputListener processListener = new OutputListener() {
            @Override
            public void processTerminated(@NotNull ProcessEvent processEvent) {
                super.processTerminated(processEvent);
                Output configInstallOut = getOutput();
                if (configInstallOut.getExitCode() == 0) {
                    setConfigInstallRes("Success", true);
                } else {
                    setConfigInstallRes(Utils.getLastLine(configInstallOut.getStdout()), false);
                }
            }
        };
        new ConfigInstall(this.project, processListener, location).run();
    }

    private void setConfigInstallRes(String results, boolean isSuccess) {
        configInstallRes.setForeground(isSuccess ? JBColor.GREEN : JBColor.RED);
        configInstallRes.setText(results);
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void reset() {
        configInstallLocation.setText(PersistencyUtils.getValue(CONFIG_INSTALL_LOCATION));
        if (!project.isDefault()) {
            installArgs.setText(ConanProjectSettings.getInstance(project).getInstallArgs());
            conanPath.setText(ConanProjectSettings.getInstance(project).getConanPath());
        }
    }

    @Override
    public void apply() {
        PersistencyUtils.setValue(CONFIG_INSTALL_LOCATION, configInstallLocation.getText());
        PersistencyUtils.setValue(CONFIG_CONAN_EXE_PATH, conanPath.getText());
        if (!project.isDefault()) {
            ConanProjectSettings.getInstance(project).setInstallArgs(installArgs.getText());
            ConanProjectSettings.getInstance(project).setConanPath(conanPath.getText());
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Config install location");
        rootPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        configInstallLocation = new JBTextField();
        rootPanel.add(configInstallLocation, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        installConfigButton = new JButton();
        installConfigButton.setText("Install");
        rootPanel.add(installConfigButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        configInstallRes = new JLabel();
        configInstallRes.setText("");
        rootPanel.add(configInstallRes, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        installArgs = new JBTextField();
        rootPanel.add(installArgs, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        installArgsLabel = new JLabel();
        installArgsLabel.setRequestFocusEnabled(true);
        installArgsLabel.setText("Install args");
        rootPanel.add(installArgsLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
