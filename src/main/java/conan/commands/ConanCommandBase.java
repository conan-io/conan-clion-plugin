package conan.commands;

import com.google.common.collect.Lists;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import conan.utils.Utils;
import org.jetbrains.annotations.Nullable;

import static conan.utils.Utils.CONAN_HOME_ENV;

/**
 * Base class for all Conan commands.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
abstract class ConanCommandBase implements Runnable {

    GeneralCommandLine args;

    ConanCommandBase(@Nullable Project project, String... args) {
        this.args = new GeneralCommandLine(Lists.asList("conan", args));

        if (project != null) {
            this.args.setWorkDirectory(project.getBaseDir().getCanonicalPath());
        }

        String conanHome = Utils.getConanHomeEnv();
        if (conanHome != null) {
            this.args.withEnvironment(CONAN_HOME_ENV, conanHome);
        }
    }

    public void addParameter(String arg) {
        args.addParameter(arg);
    }
}
