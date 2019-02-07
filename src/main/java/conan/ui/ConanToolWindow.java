package conan.ui;

import com.google.common.collect.Maps;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ui.UIUtil;
import conan.commands.IsInstalledCommand;
import conan.persistency.settings.ConanProjectSettings;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import conan.profiles.ProfileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConanToolWindow implements Disposable {

    private static final Icon CONAN_ICON = IconLoader.getIcon("/icons/conan.png");
    private static final String[] CONAN_ACTIONS = {"ReloadProject", "Install", "UpdateAndInstall", "CleanCache", "MatchProfiles", "CleanConsole", "OpenConfig"};
    private ContentManager contentManager;
    private Map<ConanProfile, ConsoleView> conanProfileContexts = Maps.newConcurrentMap();

    void initToolWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        contentManager = toolWindow.getContentManager();
        recreateContent(project);
    }

    /**
     * Remove old tabs if exists and create all necessary tabs.
     * @param project Intellij project.
     */
    public void recreateContent(@NotNull Project project) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        conanProfileContexts = Maps.newConcurrentMap();
        contentManager.removeAllContents(true);
        if (!isSupported(project)) {
            Content content = createTab(contentFactory, createUnsupportedView("Could not find Conan client in path."), "");
            contentManager.addContent(content);
            return;
        }
        createConanProfilesTabs(project, contentFactory);
        if (contentManager.getContentCount() == 0) {
            Content content = createTab(contentFactory, createUnsupportedView("Please match between CMake profiles and Conan profiles in order to use Conan in this project."), "");
            contentManager.addContent(content);
        }
    }

    /**
     * Create tabs of Conan profiles.
     * @param project Intellij project.
     * @param contentFactory the content factory used to create the tabs.
     */
    private void createConanProfilesTabs(@NotNull Project project, ContentFactory contentFactory) {
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(project);
        Map<CMakeProfile, ConanProfile> profileMatching = conanProjectSettings.getProfileMapping();
        for (ConanProfile conanProfile : ProfileUtils.getConanProfiles()) {
            if (!profileMatching.containsValue(conanProfile)) {
                continue;
            }
            ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Content content = createTab(contentFactory, consoleView.getComponent(), conanProfile.getName());
            conanProfileContexts.put(conanProfile, consoleView);
            contentManager.addContent(content);
        }
    }

    /**
     * Create a tab with conan icon.
     * @param contentFactory the content factory used to create the tabs.
     * @param component the content of the tab.
     * @param title the title of the tab.
     * @return the tab.
     */
    private Content createTab(ContentFactory contentFactory, JComponent component, String title) {
        Content content = contentFactory.createContent(createToolWindowComponents(component), title, false);
        content.setIcon(CONAN_ICON);
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        return content;
    }

    /**
     * Create a component with toolbar and content.
     * @param content the content of the tab.
     * @return the component.
     */
    private JComponent createToolWindowComponents(JComponent content) {
        SimpleToolWindowPanel filterPanel = new SimpleToolWindowPanel(false);
        filterPanel.setToolbar(createActionToolbar().getComponent());
        filterPanel.setContent(content);
        return filterPanel;
    }

    /**
     * Create the action tool bar with all Conan actions.
     * @return the action tool bar with all Conan actions.
     */
    private static ActionToolbar createActionToolbar() {
        DefaultActionGroup defaultActionGroup = new DefaultActionGroup();
        addActions(defaultActionGroup);
        return ActionManager.getInstance().createActionToolbar(ActionPlaces.CHANGES_VIEW_TOOLBAR, defaultActionGroup, false);
    }

    /**
     * Add Conan actions to the tool bar.
     * @param actionGroup the action container.
     */
    private static void addActions(DefaultActionGroup actionGroup) {
        ActionManager actionManager = ActionManager.getInstance();
        for (String action : CONAN_ACTIONS) {
            actionGroup.addAction(actionManager.getAction("Conan." + action));
        }
    }

    /**
     * Get a process handler and redirect its output to the console.
     * @param processHandler the process handler.
     * @param message a message to print in the beginning of the process.
     * @param conanProfile if specified, redirect the output to this Conan profile tab. Otherwise, redirect to the selected tab.
     */
    public void attachConsoleToProcess(ProcessHandler processHandler, String message, ConanProfile conanProfile) {
        if (conanProfile == null) {
            conanProfile = new ConanProfile(getSelectedTab());
        }
        ConsoleView consoleView = conanProfileContexts.get(conanProfile);
        if (consoleView != null) {
            consoleView.print(message + "\n\n", ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.attachToProcess(processHandler);
        }
    }

    public String getSelectedTab() {
        Content selectedContent = contentManager.getSelectedContent();
        return selectedContent == null ? "" : selectedContent.getDisplayName();
    }

    public void cleanConsole() {
        ConanProfile conanProfile = new ConanProfile(getSelectedTab());
        ConsoleView consoleView = conanProfileContexts.get(conanProfile);
        consoleView.clear();
    }

    private boolean isSupported(Project project) {
        IsInstalledCommand isInstalled = new IsInstalledCommand(project);
        isInstalled.run();
        return isInstalled.isInstalled();
    }

    /**
     * Create a panel with "unsupported view" message.
     * @param message the message to show.
     * @return a panel with "unsupported view" message.
     */
    private JPanel createUnsupportedView(String message) {
        JLabel label = new JBLabel();
        label.setText(message);
        JBPanel panel = new JBPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(label, c);
        panel.setBackground(UIUtil.getTableBackground());
        return panel;
    }

    @Override
    public void dispose() {
        conanProfileContexts.values().forEach(Disposable::dispose);
    }
}
