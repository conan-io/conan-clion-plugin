package conan.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.util.ui.ConfirmationDialog;
import conan.ui.ConanConfirmDialog;

/**
 * Clean conan cache.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CleanCacheAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        if (project != null) {
            if (!ActionUtils.isConanInstalled(project)) {
                return;
            }
            String message = "This will remove the Conan local cache, Are you sure?";
            boolean result = new ConanConfirmDialog("Removing Conan Cache", message).showAndGet();
            if(result) {
                // user pressed ok
                new conan.commands.CleanCache(project).run();
            }
        }
    }
}
