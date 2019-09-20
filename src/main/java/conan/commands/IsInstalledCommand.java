package conan.commands;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;

/**
 * Check if Conan installed and the project contains conanfile.txt file.
 */
public class IsInstalledCommand {

    private static final Logger logger = Logger.getInstance(IsInstalledCommand.class);

    /**
     * Return true iff Conan executable exists in env path.
     * @return true iff Conan executable exists in env path.
     */
    public static boolean isInstalled(Project project) {
        Version command = new Version(project);
        String version = command.run_sync();
        return version != null && !StringUtils.isBlank(version);
    }
}
