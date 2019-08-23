package conan.extensions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ShutDownTracker;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.cidr.PredefinedVariables;
import com.jetbrains.cidr.cpp.cmake.CMakeRunnerStep;
import com.jetbrains.cidr.cpp.cmake.model.CMakeModelConfigurationData;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment;
import com.jetbrains.cidr.lang.toolchains.CidrToolEnvironment;
import com.jetbrains.cidr.system.HostMachine;
import com.jetbrains.cidr.toolchains.CidrToolsUtil;
import conan.commands.ConanCommandBase;
import conan.commands.Install;
import conan.persistency.settings.ConanProjectSettings;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

/**
 * Extension point to CMake runner step.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CMakeRunnerStepImpl implements CMakeRunnerStep {

    /**
     * Run Conan install before CMake generation.
     * @param project context project for which the generation is done.
     * @param parameters See {@link Parameters}.
     */
    @Override
    public void beforeGeneration(@NotNull Project project, @NotNull CMakeRunnerStep.Parameters parameters) {
        CMakeProfile cmakeProfile = getCMakeProfile(project, parameters);
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(project);
        Map<CMakeProfile, ConanProfile> profileMatching = conanProjectSettings.getProfileMapping();
        if (cmakeProfile == null || !profileMatching.containsKey(cmakeProfile)) {
            return;
        }
        ConanProfile conanProfile = profileMatching.get(cmakeProfile);
        if (StringUtils.isNotBlank(conanProfile.getName())) {
            ConanCommandBase conanCommand = new Install(project, cmakeProfile, conanProfile, false);

            // TODO: Change background task message to inform the user that Conan is running
            // We need to run that command in the same environment as CMake
            GeneralCommandLine commandLine = conanCommand.getCommandLine();
            commandLine.withParentEnvironmentType(parameters.isPassSystemEnvironment() ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE);

            Iterator var12 = PredefinedVariables.getIDEVariables().iterator();
            while(var12.hasNext()) {
                String var11 = (String)var12.next();
                commandLine.withEnvironment(var11, "TRUE");
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
                Runnable runnable = new Runnable() {
                    public void run() {
                        hostMachine.killProcessTree(baseProcessHandler);
                    }
                };
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

    /**
     * Extract {@link CMakeProfile} profile from {@link Parameters}.
     * @param project context project for which the generation is done.
     * @param parameters See {@link Parameters}.
     * @return {@link CMakeProfile} profile from {@link Parameters}.
     */
    private CMakeProfile getCMakeProfile(@NotNull Project project, @NotNull CMakeRunnerStep.Parameters parameters) {
        CMakeWorkspace ws = CMakeWorkspace.getInstance(project);
        for (CMakeModelConfigurationData data : ws.getModelConfigurationData()) {
            if (FileUtil.filesEqual(data.getGenerationDir(), parameters.getOutputDir().toFile())) {
                return new CMakeProfile(data.getConfigName(), parameters.getOutputDir().toFile());
            }
        }
        return null;
    }
}
