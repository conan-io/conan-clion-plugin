package conan.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

/**
 * Clean conan cache.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CleanCacheAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        if (getEventProject(anActionEvent) != null) {
            new conan.commands.CleanCache(anActionEvent.getProject()).run();
        }
    }
}
