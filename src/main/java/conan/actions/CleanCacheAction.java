package conan.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import conan.commands.RemoveAllForce;
import conan.ui.ConanConfirmDialog;

/**
 * Clean conan cache.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CleanCacheAction extends AnAction implements DumbAware {

    private static final String WIPE_CACHE_CONFIRM_MESSAGE = "This will remove the Conan local cache, Are you sure?";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        if (project == null || !ActionUtils.isConanInstalled(project)) {
            return;
        }
        boolean result = new ConanConfirmDialog("Removing Conan Cache", WIPE_CACHE_CONFIRM_MESSAGE).showAndGet();
        if (result) {
            // user pressed ok
            new RemoveAllForce(project).run(null);
        }
    }
}
