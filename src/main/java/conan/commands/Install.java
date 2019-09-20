package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.CMakeRunner;
import conan.persistency.settings.ConanProjectSettings;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Install conan packages.
 * <p>
 * Run "conan install . if=<cmake-target-dir> -pr=<Conan-profile> [Extra arguments]"
 * or
 * "conan install . if=<cmake-target-dir> -pr=<Conan-profile> --update [Extra arguments]"
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Install extends ConanCommandBase {

    public Install(Project project, CMakeProfile cMakeProfile, ConanProfile conanProfile, boolean update) {
        super(project, "install", project.getBasePath(), "-if=" + cMakeProfile.getTargetDir(), "-pr=" + conanProfile.getName());
        addArguments(project, update);
    }

    private void addArguments(Project project, boolean update) {
        if (update) {
            addParameter("--update");
        }
        String installArgs = ConanProjectSettings.getInstance(project).getInstallArgs();
        String[] tokens = StringUtils.split(installArgs);
        if (ArrayUtils.isNotEmpty(tokens)) {
            Arrays.stream(tokens).forEach(super::addParameter);
        }
    }

}
