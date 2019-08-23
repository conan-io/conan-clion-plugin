package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;

/**
 * Clean Conan cache.
 * Run "conan remove * -f"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CleanCache extends ConanCommandBase {
    public CleanCache(Project project) {
        super(project, "remove", "*", "-f");
    }

    public void run(ProcessListener processListener) {
        AsyncConanCommand asyncCommand = new AsyncConanCommand(this, null, processListener);
        asyncCommand.run();
    }

}
