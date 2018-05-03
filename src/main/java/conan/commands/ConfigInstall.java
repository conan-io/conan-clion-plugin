package conan.commands;

import com.intellij.execution.process.ProcessListener;

/**
 * Download and install Conan configuration.
 * Run "conan config install <url>"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConfigInstall extends AsyncConanCommand {

    public ConfigInstall(ProcessListener processListener, String url) {
        super(null, "", null, processListener, "config", "install", url);
    }
}
