package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;

/**
 * Download and install Conan configuration.
 * Run "conan config install <url>"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConfigInstall extends AsyncConanCommand {

    public ConfigInstall(Project project, ProcessListener processListener, String location) {
        super(project, null, processListener, "config", "install", location);
    }
}
