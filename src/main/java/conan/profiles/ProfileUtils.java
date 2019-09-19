package conan.profiles;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import conan.commands.Config;
import conan.commands.ProfileList;

import java.util.List;

/**
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ProfileUtils {

    /**
     * Extract CMake profiles from the project configuration.
     * @param project Intellij project
     * @return list of CMake profiles from the project configuration.
     */
    public static List<CMakeProfile> getCmakeProfiles(Project project) {
        List<CMakeProfile> profiles = Lists.newArrayList();
        CMakeWorkspace ws = CMakeWorkspace.getInstance(project);
        if (ws.isInitialized()) {
            ws.getModelConfigurationData().forEach(config -> profiles.add(new CMakeProfile(config.getConfigName(), config.getGenerationDir())));
        }
        return profiles;
    }

    /**
     * Extract Conan profiles.
     * @return list of Conan profiles.
     */
    public static List<ConanProfile> getConanProfiles(Project project) {
        List<ConanProfile> profiles = Lists.newArrayList();
        // Prevents "Remotes registry file missing" message
        new Config(project).run();
        new ProfileList(project).run(profiles);
        return profiles;
    }

}
