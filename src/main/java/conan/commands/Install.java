package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.CMakeRunner;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;

/**
 * Install conan packages.
 *
 * Run "conan install . if=<cmake-target-dir> -pr=<Conan-profile>"
 * or
 * "conan install . if=<cmake-target-dir> -pr=<Conan-profile> --update"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Install extends AsyncConanCommand {

    private static final String INSTALL_MESSAGE = "Installing CMake profile ";

    public Install(Project project, CMakeProfile cMakeProfile, ConanProfile conanProfile, boolean update) {
        this(project, (ProcessListener) null, cMakeProfile, conanProfile, update);
    }

    public Install(Project project, ProcessListener processListener, CMakeProfile cMakeProfile, ConanProfile conanProfile, boolean update) {
        super(project, INSTALL_MESSAGE + cMakeProfile.getName(), conanProfile, processListener, "install", project.getBasePath(), "-if=" + cMakeProfile.getTargetDir(), "-pr=" + conanProfile.getName());
        if (update) {
            addParameter("--update");
        }
    }

    public Install(Project project, CMakeRunner.Listener listener, CMakeProfile cMakeProfile, ConanProfile conanProfile, boolean update) {
        super(project, INSTALL_MESSAGE + cMakeProfile.getName(), conanProfile, listener, "install", project.getBasePath(), "-if=" + cMakeProfile.getTargetDir(), "-pr=" + conanProfile.getName());
        if (update) {
            addParameter("--update");
        }
    }
}
