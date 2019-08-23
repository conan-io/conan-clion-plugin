package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;

/**
 * Download and install Conan configuration.
 * Run "conan config install <url>"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConfigInstall extends ConanCommandBase {

    public ConfigInstall(Project project, String source) {
        super(project, "config", "install", source);
    }

    public void run(ProcessListener processListener) {
        new AsyncConanCommand(this, null, processListener).run();
    }
}
