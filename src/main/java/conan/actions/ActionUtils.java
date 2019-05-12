package conan.actions;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import conan.commands.Install;
import conan.commands.IsInstalledCommand;
import conan.persistency.settings.ConanProjectSettings;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import conan.ui.ConanToolWindow;
import conan.ui.profileMatching.ProfileMatcher;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Created by Yahav Itzhak on Feb 2018.
 */
class ActionUtils {

    static boolean isConanInstalled(Project project){
        IsInstalledCommand isInstalled = new IsInstalledCommand(project);
        isInstalled.run();
        return isInstalled.isInstalled();
    }

    /**
     * Run conan install for the selected Conan profile.
     *
     * @param project   Intellij project.
     * @param component the {@link ProfileMatcher} dialog will be shown in this component position.
     * @param update    true if it's update and install action.
     */
    static void runInstall(Project project, Component component, boolean update) {
        if (!isConanInstalled(project)){
            return;
        }
        FileDocumentManager.getInstance().saveAllDocuments();
        ConanToolWindow conanToolWindow = ServiceManager.getService(project, ConanToolWindow.class);
        ConanProfile conanProfile = new ConanProfile(conanToolWindow.getSelectedTab());
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(project);
        List<CMakeProfile> cMakeProfiles = getMatchedCMakeProfiles(conanProjectSettings, conanProfile);
        if (StringUtils.isBlank(conanProfile.getName())) {
            matchProfilesAndInstall(project, component, update);
            return;
        }
        cMakeProfiles.forEach(cMakeProfile -> new Install(project, cMakeProfile, conanProfile, update).run());
    }

    /**
     * In case the user clicks on install and no Conan-CMake matching exist, we open the matching dialog for him.
     *
     * @param project   Intellij project.
     * @param component the {@link ProfileMatcher} dialog will be shown in this component position.
     * @param update    true if it's update and install action.
     */
    private static void matchProfilesAndInstall(Project project, Component component, boolean update) {
        if (!isConanInstalled(project)){
            return;
        }
        ProfileMatcher.showDialog(project, component);
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(project);
        Map<CMakeProfile, ConanProfile> profileMapping = conanProjectSettings.getProfileMapping();
        profileMapping.forEach((cMakeProfile, conanProfile) -> {
            if (StringUtils.isNotBlank(conanProfile.getName())) {
                new Install(project, cMakeProfile, conanProfile, update).run();
            }
        });
    }

    /**
     * Get the matched CMake profiles for the selected Conan profile.
     *
     * @param conanProjectSettings the Conan project settings.
     * @param conanProfile         the conan profile to match.
     * @return list of matched CMake profiles for the selected Conan profile.
     */
    private static java.util.List<CMakeProfile> getMatchedCMakeProfiles(ConanProjectSettings conanProjectSettings, ConanProfile conanProfile) {
        List<CMakeProfile> cmakeProfiles = Lists.newArrayList();
        Map<CMakeProfile, ConanProfile> profileMatching = conanProjectSettings.getProfileMapping();
        for (Map.Entry<CMakeProfile, ConanProfile> matching : profileMatching.entrySet()) {
            if (matching.getValue().equals(conanProfile)) {
                cmakeProfiles.add(matching.getKey());
            }
        }
        return cmakeProfiles;
    }
}
