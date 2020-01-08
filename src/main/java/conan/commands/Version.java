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

    public Version(String conanPath, Project project) {
        super(conanPath, project, "--version");
        this.args.withEnvironment("CONAN_COLOR_DISPLAY", "0");
    }

    public Version(Project project) {
        super(project, "--version");
        this.args.withEnvironment("CONAN_COLOR_DISPLAY", "0");
    }

    public String run_sync() {
        VersionProcessAdapter adapter = new VersionProcessAdapter();
        super.run_sync(adapter);
        return adapter.conanVersion;
    }
}
