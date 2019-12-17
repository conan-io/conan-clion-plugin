package conan.ui.configuration;

import com.intellij.execution.Output;
import com.intellij.execution.OutputListener;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
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
import conan.commands.ConfigInstall;
import conan.commands.Version;
import conan.persistency.PersistencyUtils;
import conan.persistency.settings.ConanProjectSettings;
import conan.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;

import static conan.persistency.Keys.*;

/**
 * Represents the Conan settings form.
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConanConfig implements Configurable {

    private static final Logger logger = Logger.getInstance(ConanConfig.class);

    public static final String CONFIG_NAME = "Conan";
    private Project project;
    private JPanel rootPanel;

    private JBTextField configInstallSource;
    private JButton installConfigButton;
    private JLabel configInstallRes;

    private JLabel conanPathLabel;
    private TextFieldWithBrowseButton conanPath;
    private JButton conanPathCheck;
    private JLabel conanPathValidate;

    private JCheckBox checkUpdate;
    private JComboBox buildPolicy;
    private JCheckBox advancedConfig;
    private JTextField installArgs;
    private JLabel installArgsLabel;
    private JPanel panelWorkingEnvironment;
    private JPanel panelInstallCommand;
    private JPanel panelConanConfiguration;

    public ConanConfig(@NotNull Project project) {
        this.project = project;
        if (project.isDefault()) { // No project
            panelWorkingEnvironment.setVisible(false);
            panelInstallCommand.setVisible(false);
            panelConanConfiguration.setVisible(false);

            advancedConfig.setVisible(false);
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
        conanPathValidate.setVisible(false);
        configInstallRes.setVisible(false);

        installConfigButton.addActionListener(actionEvent -> {
            String source = configInstallSource.getText();
            if (StringUtils.isBlank(source)) {
                source = PersistencyUtils.getValue(CONFIG_INSTALL_SOURCE);
            }
            PersistencyUtils.setValue(CONFIG_INSTALL_SOURCE, source);

            if (!(Utils.validateUrl(source) || new File(source).exists())) {
                setConfigInstallRes("Invalid URL or path", false);
                return;
            }
            runConfigInstall(source);
        });
        configInstallSource.getEmptyText().setText("Git repository, local folder or ZIP file (local or http)");

        // Install arguments
        ConanProjectSettings.buildPolicies.forEach(item -> buildPolicy.addItem(item));
        //installArgs.getEmptyText().setText("Arguments other than '--if', '--pr' and '--update'");

        // Path to Conan
        String envExePath = System.getenv("CONAN_EXE_PATH");
        if (envExePath != null) {
            conanPath.setText(envExePath);
        }
        conanPath.setToolTipText("Path to the Conan executable (default: 'conan' into PATH)");
        conanPath.addActionListener(actionEvent -> {
            final FileChooserDescriptor d = FileChooserDescriptorFactory.createSingleFileDescriptor();
            VirtualFile initialFile = StringUtil.isNotEmpty(conanPath.getText()) ? LocalFileSystem.getInstance().findFileByPath(conanPath.getText()) : null;
            VirtualFile file = FileChooser.chooseFile(d, project, initialFile);
            if (file != null) {
                conanPath.setText(file.getCanonicalPath());
            }
            this.validateConanPath();
            logger.info("Chosen file for conanPath: " + conanPath.getText());
        });
        conanPathCheck.addActionListener(actionEvent -> this.validateConanPath());

        // Advanced configuration
        advancedConfig.addActionListener(actionEvent -> {
            toggleAdvancedConfig(advancedConfig.isSelected());
        });

        return rootPanel;
    }

    /**
     * Run {@link ConfigInstall}.
     *
     * @param source the source of the Conan configuration.
     */
    private void runConfigInstall(String source) {
        OutputListener processListener = new OutputListener() {
            @Override
            public void processTerminated(@NotNull ProcessEvent processEvent) {
                super.processTerminated(processEvent);
                Output configInstallOut = getOutput();
                if (configInstallOut.getExitCode() == 0) {
                    setConfigInstallRes("Config installed!", true);
                } else {
                    setConfigInstallRes(Utils.getLastLine(configInstallOut.getStdout()), false);
                }
            }
        };
        new ConfigInstall(this.project, source).run_async(null, null, processListener);
    }

    private void setConfigInstallRes(String results, boolean isSuccess) {
        configInstallRes.setVisible(true);
        configInstallRes.setForeground(isSuccess ? JBColor.GREEN : JBColor.RED);
        configInstallRes.setText(results);
    }

    private void validateConanPath() {
        String testPath = conanPath.getText();
        testPath = testPath.isEmpty() ? "conan" : testPath;
        String v = new Version(testPath, this.project).run_sync();
        conanPathValidate.setVisible(true);
        if (v != null) {
            conanPathValidate.setForeground(JBColor.BLACK);
            conanPathValidate.setText(v);
        } else {
            conanPathValidate.setForeground(JBColor.RED);
            conanPathValidate.setText("Conan client not found!");
        }
    }

    private void toggleAdvancedConfig(boolean checked) {
        installArgs.setVisible(checked);
        installArgsLabel.setVisible(checked);
        if (!checked) {
            // We really want to get rid of `installArgs` free text field
            installArgs.setText("");
        }
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void reset() {
        configInstallSource.setText(PersistencyUtils.getValue(CONFIG_INSTALL_SOURCE));
        conanPath.setText(PersistencyUtils.getValue(CONFIG_CONAN_EXE_PATH));

        checkUpdate.setSelected(PersistencyUtils.getValue(CONFIG_INSTALL_CHECK_UPDATE).equals("true"));
        buildPolicy.setSelectedItem(PersistencyUtils.getValue(CONFIG_INSTALL_BUILD_POLICY));
        installArgs.setText(PersistencyUtils.getValue(CONFIG_INSTALL_ARGS));

        advancedConfig.setSelected(PersistencyUtils.getValue(CONFIG_ADVANCED_CONFIG).equals("true"));

        if (!project.isDefault()) {
            conanPath.setText(ConanProjectSettings.getInstance(project).getConanPath());

            checkUpdate.setSelected(ConanProjectSettings.getInstance(project).getInstallUpdate());
            buildPolicy.setSelectedItem(ConanProjectSettings.getInstance(project).getInstallBuildPolicy());
            String settingsInstallArgs = ConanProjectSettings.getInstance(project).getConfigInstallArgs();
            if (!settingsInstallArgs.isEmpty()) {
                installArgs.setText(settingsInstallArgs);
                advancedConfig.setSelected(true);
            }
            else {
                advancedConfig.setSelected(false);
            }
        }
    }

    @Override
    public void apply() {
        PersistencyUtils.setValue(CONFIG_INSTALL_SOURCE, configInstallSource.getText());
        PersistencyUtils.setValue(CONFIG_CONAN_EXE_PATH, conanPath.getText());

        PersistencyUtils.setValue(CONFIG_INSTALL_CHECK_UPDATE, checkUpdate.isSelected() ? "true" : "false");
        PersistencyUtils.setValue(CONFIG_INSTALL_BUILD_POLICY, buildPolicy.getSelectedItem().toString());
        PersistencyUtils.setValue(CONFIG_INSTALL_ARGS, installArgs.getText());

        PersistencyUtils.setValue(CONFIG_ADVANCED_CONFIG, advancedConfig.isSelected() ? "true" : "false");
        if (!project.isDefault()) {
            ConanProjectSettings.getInstance(project).setConanPath(conanPath.getText());

            ConanProjectSettings.getInstance(project).setInstallUpdate(checkUpdate.isSelected());
            ConanProjectSettings.getInstance(project).setInstallBuildPolicy(buildPolicy.getSelectedItem().toString());
            if (advancedConfig.isSelected() && !installArgs.getText().isEmpty()) {
                ConanProjectSettings.getInstance(project).setConfigInstallArgs(installArgs.getText());
            }
            else {
                ConanProjectSettings.getInstance(project).setConfigInstallArgs("");
            }
        }
        this.validateConanPath();
    }

}
