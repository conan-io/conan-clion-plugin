package conan.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

import static conan.actions.ActionUtils.runInstall;

/**
 * Download Conan dependencies for the project and the selected Conan profile.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class InstallAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        if (project == null) {
            return;
        }
        runInstall(project, anActionEvent.getInputEvent().getComponent(), false);
    }
}
