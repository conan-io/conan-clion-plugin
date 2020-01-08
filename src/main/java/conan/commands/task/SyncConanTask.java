package conan.commands.task;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import conan.ui.ConanToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static conan.utils.Utils.log;

/**
 * Run Conan task in foreground.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class SyncConanTask extends Task.Modal {

    private static final Logger logger = Logger.getInstance(SyncConanTask.class);
    private GeneralCommandLine args;
    private ProcessListener processListener;

    public SyncConanTask(Project project, @Nullable ProcessListener processListener, GeneralCommandLine args) {
        super(project, "Running Conan...", true);
        this.processListener = processListener;
        this.args = args;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        String commandLineString = args.getCommandLineString();
        try {
            log(logger, commandLineString, "", NotificationType.INFORMATION);
            ProcessHandler processHandler = new OSProcessHandler(args);
            processHandler.startNotify();
            if (processListener != null) {
                processHandler.addProcessListener(processListener);
            } else {
                ConanToolWindow conanToolWindow = ServiceManager.getService(getProject(), ConanToolWindow.class);
                conanToolWindow.attachConsoleToProcess(processHandler, commandLineString, null);
            }
            processHandler.waitFor();
        } catch (ExecutionException e) {
            log(logger, "Error running Conan command: '" + commandLineString + "'", e.getMessage(), NotificationType.ERROR);
        }
    }
}
