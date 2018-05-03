package conan.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static conan.utils.Utils.log;

/**
 * Check if Conan installed and the project contains conanfile.txt file.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class IsInstalledCommand extends ConanCommandBase {

    private static final Logger logger = Logger.getInstance(IsInstalledCommand.class);
    private Project project;
    private boolean isInstalled;

    public IsInstalledCommand(Project project) {
        super(project);
        this.project = project;
    }

    @Override
    public void run() {
        if (!isConanInstalled()) {
            log(logger, "Conan is not installed", "", NotificationType.INFORMATION);
            return;
        }
        if (!isConanFileExists()) {
            log(logger, "conanfile.py and conanfile.txt doesn't exist in project base dir", "", NotificationType.INFORMATION);
            return;
        }
        isInstalled = true;
    }

    /**
     * Return true iff Conan executable exists in env path.
     * @return true iff Conan executable exists in env path.
     */
    private boolean isConanInstalled() {
        ProcessHandler processHandler;
        try {
            processHandler = new OSProcessHandler(args);
            processHandler.startNotify();
            return processHandler.waitFor();
        } catch (ExecutionException e) {
            log(logger, e.getMessage(), Arrays.toString(e.getStackTrace()), NotificationType.INFORMATION);
            return false;
        }
    }

    /**
     * Return true iff conanfile.txt exists in project base directory.
     * @return true iff conanfile.txt exists in project base directory.
     */
    private boolean isConanFileExists() {
        if (project.getBasePath() == null) {
            return false;
        }
        Path conanPyFile = Paths.get(project.getBasePath(), "conanfile.py");
        Path conanTxtFile = Paths.get(project.getBasePath(), "conanfile.txt");
        return conanPyFile.toFile().exists() || conanTxtFile.toFile().exists();
    }

    public boolean isInstalled() {
        return isInstalled;
    }
}
