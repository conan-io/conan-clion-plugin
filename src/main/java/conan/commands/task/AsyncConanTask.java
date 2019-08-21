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
import com.jetbrains.cidr.cpp.cmake.CMakeRunner;
import conan.profiles.ConanProfile;
import conan.ui.ConanToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static conan.utils.Utils.log;

/**
 * Run Conan task in background.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class AsyncConanTask extends Task.Backgroundable {

    private static final Logger logger = Logger.getInstance(AsyncConanTask.class);
    private GeneralCommandLine args;
    private ConanProfile conanProfile;
    private CMakeRunner.Listener cmakeListener;
    private ProcessListener processListener;

    public AsyncConanTask(@Nullable Project project, ConanProfile conanProfile, CMakeRunner.Listener cmakeListener, ProcessListener processListener, GeneralCommandLine args) {
        super(project, "Running Conan...");
        this.conanProfile = conanProfile;
        this.processListener = processListener;
        this.cmakeListener = cmakeListener;
        this.args = args;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        try {
            String message = args.getCommandLineString();
            log(logger, message, "", NotificationType.INFORMATION);
            ProcessHandler processHandler = new OSProcessHandler(args);
            if (processListener != null) {
                processHandler.addProcessListener(processListener);
            } else if (cmakeListener != null) {
                cmakeListener.processStarted(processHandler);
            } else if (getProject() != null){
                ConanToolWindow conanToolWindow = ServiceManager.getService(getProject(), ConanToolWindow.class);
                conanToolWindow.attachConsoleToProcess(processHandler, message, conanProfile);
            }
            processHandler.startNotify();
            processHandler.waitFor();
        } catch (ExecutionException e) {
            log(logger, e.getMessage(), Arrays.toString(e.getStackTrace()), NotificationType.ERROR);
        }
    }
}
