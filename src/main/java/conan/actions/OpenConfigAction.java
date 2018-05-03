package conan.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import conan.ui.configuration.ConanConfig;

/**
 * Open Conan configuration.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class OpenConfigAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        if (project == null) {
            return;
        }
        ShowSettingsUtil.getInstance().showSettingsDialog(project, ConanConfig.CONFIG_NAME);
    }
}
