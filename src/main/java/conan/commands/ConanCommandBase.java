package conan.commands;

import com.google.common.collect.Lists;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import conan.persistency.settings.ConanProjectSettings;
import conan.utils.Utils;

import static conan.utils.Utils.CONAN_HOME_ENV;
import static conan.utils.Utils.log;

/**
 * Base class for all Conan commands.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
abstract class ConanCommandBase implements Runnable {

    private static final Logger logger = Logger.getInstance(ConanCommandBase.class);

    GeneralCommandLine args;

    ConanCommandBase(Project project, String... args) {
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(project);
        String envExePath = System.getenv("CONAN_EXE_PATH");
        if(envExePath != null){
            log(logger, "Conan at: ", envExePath, NotificationType.INFORMATION);
            this.args = new GeneralCommandLine(Lists.asList(envExePath, args));
        }
        else {
            String configPath = conanProjectSettings.getConanPath();
            String conanExe = StringUtil.isEmpty(configPath) ? "conan" : configPath;
            log(logger, "Conan at: ", conanExe, NotificationType.INFORMATION);
            this.args = new GeneralCommandLine(Lists.asList(conanExe, args));
        }
        this.args.setWorkDirectory(project.getBaseDir().getCanonicalPath());
        String conanHome = Utils.getConanHomeEnv();
        if (conanHome != null) {
            this.args.withEnvironment(CONAN_HOME_ENV, conanHome);
        }
    }

    public void addParameter(String arg) {
        args.addParameter(arg);
    }
}
