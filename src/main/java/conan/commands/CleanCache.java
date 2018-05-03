package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;

/**
 * Clean Conan cache.
 * Run "conan remove * -f"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CleanCache extends AsyncConanCommand {
    public CleanCache(Project project) {
        super(project, "Cleaning Conan cache", "remove", "*", "-f");
    }

    public CleanCache(ProcessListener processListener) {
        super(null, "", null, processListener,"remove", "*", "-f");
    }
}
