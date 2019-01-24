package conan.commands;

import com.intellij.openapi.project.Project;

/**
 * Print conan version.
 * Run "conan --version"
 * <p>
 * Created by Yahav Itzhak on Jan 2019.
 */
public class Version extends SyncConanCommand {
    public Version(Project project) {
        super(project, null, "-v");
    }
}
