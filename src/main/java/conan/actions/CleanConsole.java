package conan.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import conan.ui.ConanToolWindow;

/**
 * Clean the console of the selected Conan profile.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CleanConsole extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        if (project == null) {
            return;
        }
        ConanToolWindow conanToolWindow = ServiceManager.getService(project, ConanToolWindow.class);
        conanToolWindow.cleanConsole();
    }
}
