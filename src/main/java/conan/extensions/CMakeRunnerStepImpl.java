package conan.extensions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.cidr.cpp.cmake.CMakeRunnerStep;
import com.jetbrains.cidr.cpp.cmake.model.CMakeModelConfigurationData;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import conan.commands.Install;
import conan.persistency.settings.ConanProjectSettings;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

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
            new Install(project, parameters.getListener(), cmakeProfile, conanProfile, false).run();
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
