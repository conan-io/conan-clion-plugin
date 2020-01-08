package conan.commands;

import com.intellij.openapi.project.Project;
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
        super(project, "install", project.getBasePath(), "--install-folder=" + cMakeProfile.getTargetDir(), "--profile=" + conanProfile.getName());
        addArguments(project, update);
    }

    private void addArguments(Project project, boolean update) {
        String installArgs = ConanProjectSettings.getInstance(project).getInstallArgs();
        if (update && !installArgs.contains("update")) {
            addParameter("--update");
        }
        String[] tokens = StringUtils.split(installArgs);
        if (ArrayUtils.isNotEmpty(tokens)) {
            Arrays.stream(tokens).forEach(super::addParameter);
        }
    }

}
