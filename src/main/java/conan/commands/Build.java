package conan.commands;

import com.intellij.openapi.project.Project;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;

public class Build extends ConanCommandBase {
    public Build(Project project, CMakeProfile cMakeProfile, ConanProfile conanProfile, boolean configure) {
        super(project, "build", project.getBasePath(), "-bf=" + cMakeProfile.getTargetDir());
        if (configure) {
            addParameter("--configure");
        }
    }
}
