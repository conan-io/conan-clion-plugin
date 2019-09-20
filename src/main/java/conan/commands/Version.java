package conan.commands;

import com.intellij.openapi.project.Project;
import conan.commands.process_adapters.VersionProcessAdapter;

/**
 * Print conan version.
 * Run "conan --version"
 * <p>
 * Created by Yahav Itzhak on Jan 2019.
 */
public class Version extends ConanCommandBase {

    public Version(Project project) {
        super(project, "--version");
    }

    public void run_sync(String version) {
        super.run_sync(new VersionProcessAdapter(version));
    }
}
