package conan.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConanToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        ConanToolWindow conanToolWindow = ServiceManager.getService(project, ConanToolWindow.class);
        conanToolWindow.initToolWindow(project, toolWindow);
    }
}
