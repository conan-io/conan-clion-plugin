package conan.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.Arrays;

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
        ConanCommandBase command = new ConanCommandBase(project);

        ProcessHandler processHandler;
        try {
            processHandler = new OSProcessHandler(command.args);
            processHandler.startNotify();
            boolean ret = processHandler.waitFor();
            if (!ret) {
                log(logger, "Conan is not installed", "", NotificationType.INFORMATION);
            }
            return ret;
        } catch (ExecutionException e) {
            log(logger, e.getMessage(), Arrays.toString(e.getStackTrace()), NotificationType.INFORMATION);
            return false;
        }
    }
}
