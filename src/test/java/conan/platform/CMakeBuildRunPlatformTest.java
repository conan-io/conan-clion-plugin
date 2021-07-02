package conan.platform;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.jetbrains.cidr.CidrCodeInsightFixture;
import com.jetbrains.cidr.CidrTestCase;
import com.jetbrains.cidr.CidrTestDataFixture;
import com.jetbrains.cidr.cpp.CPPTestDataFixture;
import com.jetbrains.cidr.cpp.cmake.CMakeProjectFixture;
import com.jetbrains.cidr.cpp.cmake.CMakeSettings;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeProfileInfo;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.execution.CidrExecutionFixture;
import com.jetbrains.cidr.execution.debugger.CidrDebuggingFixture;
import conan.persistency.settings.ConanProjectSettings;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class CMakeBuildRunPlatformTest extends CidrTestCase<CMakeProjectFixture, CidrExecutionFixture<CMakeProjectFixture>, CidrDebuggingFixture<CidrExecutionFixture<CMakeProjectFixture>>, CidrCodeInsightFixture> {
    boolean executableRanSuccessfully = false;

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    @Nullable
    @Override
    protected CidrTestDataFixture createTestDataFixture() {
        return new CPPTestDataFixture(new File(Resources.getResource(getClass(), "fixture").getFile()));
    }

    @Nullable
    @Override
    protected CMakeProjectFixture createProjectFixture() {
        return new CMakeProjectFixture(myTestDataFixture);
    }

    private void create_conan_prorfile_matching(CMakeWorkspace cMakeWorkspace) {
        // Create profile matching for the Conan plugin
        List<CMakeSettings.Profile> cmakeProfiles = cMakeWorkspace.getSettings().getProfiles();
        Collection<CMakeProfileInfo> profileInfos = cMakeWorkspace.getProfileInfos();
        Map<CMakeProfile, ConanProfile> profileMapping = Maps.newHashMap();
        for (CMakeSettings.Profile profile : cmakeProfiles)
        {
            Optional<CMakeProfileInfo> profileInfo = profileInfos.stream().filter(pi -> pi.getProfile() == profile).findFirst();
            assert profileInfo.isPresent();

            CMakeProfile cMakeProfile = new CMakeProfile(profile.getName(), Objects.requireNonNull(profileInfo.get().getGenerationDir()));
            ConanProfile conanProfile = new ConanProfile("default");

            profileMapping.put(cMakeProfile, conanProfile);
        }
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(myProjectFixture.getProject());
        conanProjectSettings.setProfileMapping(profileMapping);
    }

    public void testBuildRun() throws Exception {
        // initProject asserts CMake configuration succeeded.
        // Ours will fail until we run Conan install.
        myProjectFixture.initProject("cmake-build-run", null, false);
        myProjectFixture.openProjectWithoutReloadingCMake();

        // Reloading creates Debug CMake profile.
        // Reloading in this way does not assert configuration was successful.
        myProjectFixture.reload();

        // Now CMake profile exists, match to a Conan profile.
        create_conan_prorfile_matching(myProjectFixture.getCMakeWorkspace());

        // Check CMake configure works.
        myProjectFixture.reload();
        myProjectFixture.assertErrors();

        // Build the test executable.
        myProjectFixture.buildConfiguration("test_exe", "Debug");

        // Create run configuration for built executable.
        CMakeAppRunConfiguration cmake_configuration = myProjectFixture.createRunConfiguration("test_exe", "Debug");
        RunManager run_manager = RunManager.getInstance(myProjectFixture.getProject());
        RunnerAndConfigurationSettings run_configuration = run_manager.findConfigurationByName(cmake_configuration.getName());
        assert run_configuration != null;

        // Create execution environment for running the target.
        // This code copied from ProgramRunnerUtil.executeConfiguration(RunnerAndConfigurationSettings, Executor).
        Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder.create(executor, run_configuration);
        ExecutionEnvironment environment = builder.activeTarget().build();

        // Create listener to check return code of run executable.
        ProcessListener check_return_listener = new ProcessListener() {
            @Override
            public void processTerminated(@NotNull ProcessEvent processEvent) {
                if (processEvent.getExitCode() == 0)
                    executableRanSuccessfully = true;
            }

            @Override
            public void startNotified(@NotNull ProcessEvent processEvent) {}
            @Override
            public void processWillTerminate(@NotNull ProcessEvent processEvent, boolean b) {}
            @Override
            public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {}
        };

        // Create callback for the runner which registers the process listener.
        ProgramRunner.Callback program_run_callback = runContentDescriptor -> {
            ProcessHandler processHandler = runContentDescriptor.getProcessHandler();
            if (processHandler == null)
                return;

            processHandler.addProcessListener(check_return_listener);
        };

        // Actually execute the run configuration with the created environment and callback.
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ProgramRunnerUtil.executeConfigurationAsync(environment, false, false, program_run_callback);
        });

        // Defaults to false in this class' definition
        // Set to true in process listener if test exe return code was zero.
        assert executableRanSuccessfully;
    }
}
