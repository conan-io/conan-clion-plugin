package conan.commands;

import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import conan.profiles.ConanProfile;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

import static conan.utils.Utils.log;

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
        String version = "";
        command.run_sync(version);
        return !StringUtils.isBlank(version);
    }
}
