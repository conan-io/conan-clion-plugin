package conan.commands.task;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.ShutDownTracker;
import com.jetbrains.cidr.PredefinedVariables;
import com.jetbrains.cidr.cpp.cmake.CMakeRunnerStep;
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment;
import com.jetbrains.cidr.lang.toolchains.CidrToolEnvironment;
import com.jetbrains.cidr.system.HostMachine;
import com.jetbrains.cidr.toolchains.CidrToolsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;


public class CMakeEnvironmentTask {

    private GeneralCommandLine commandLine;

    public CMakeEnvironmentTask(GeneralCommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public void run(@NotNull CMakeRunnerStep.Parameters parameters) {
        commandLine.withParentEnvironmentType(parameters.isPassSystemEnvironment() ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE);

        Iterator itIDEVariables = PredefinedVariables.getIDEVariables().iterator();
        while(itIDEVariables.hasNext()) {
            String variable = (String)itIDEVariables.next();
            commandLine.withEnvironment(variable, "TRUE");
        }
        commandLine.getEnvironment().putAll(parameters.getAdditionalEnvironment());
        commandLine.setWorkDirectory(parameters.getOutputDir().toFile());

        commandLine.setRedirectErrorStream(true); // TODO: Check

        CPPEnvironment environment = parameters.getEnvironment();
        try {
            environment.prepare(commandLine, CidrToolEnvironment.PrepareFor.BUILD);
            final HostMachine hostMachine = environment.getHostMachine();
            final BaseProcessHandler baseProcessHandler;
            baseProcessHandler = hostMachine.createProcess(commandLine, false, false);
            Runnable runnable = () -> hostMachine.killProcessTree(baseProcessHandler);
            ShutDownTracker.getInstance().registerShutdownTask(runnable);

            try {
                parameters.getListener().processStarted(baseProcessHandler);
                ProcessOutput processOutput = CidrToolsUtil.runWithProgress(baseProcessHandler, 0);
                if (processOutput.isCancelled()) {
                    throw new ProcessCanceledException();
                }
            }
            finally {
                ShutDownTracker.getInstance().unregisterShutdownTask(runnable);
                runnable.run();
            }
        }
        catch (ExecutionException e) {
            // TODO: What to do?
        }

    }
}
