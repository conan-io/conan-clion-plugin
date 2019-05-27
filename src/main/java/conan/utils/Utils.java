package conan.utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import conan.commands.IsInstalledCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Utils {

    public static final String CONAN_HOME_ENV = "CONAN_USER_HOME";
    private static final NotificationGroup EVENT_LOG_NOTIFIER = new NotificationGroup("CONAN_LOG", NotificationDisplayType.NONE, true);

    public static boolean validateUrl(String url) {
        UrlValidator validator = new UrlValidator();
        return validator.isValid(url);
    }

    public static String getLastLine(String string) {
        String[] lines = string.split("\n");
        return lines.length > 1 ? lines[lines.length - 1] : "";
    }

    @Nullable
    public static String getConanHomeEnv() {
        return System.getProperty(CONAN_HOME_ENV);
    }

    public static void log(Logger logger, String title, String details, NotificationType level) {
        switch (level) {
            case ERROR:
                logger.error(title, details);
                break;
            case WARNING:
                logger.warn(title + "\n" + details);
                break;
            default:
                logger.info(title + "\n" + details);
                return;
        }
        if (StringUtils.isBlank(details)) {
            details = title;
        }
        Notifications.Bus.notify(EVENT_LOG_NOTIFIER.createNotification(title, details, level, null));
    }

    /**
     * Return true if conanfile.txt exists in project base directory.
     * @param project Intellij project.
     * @return true if conanfile.txt exists in project base directory.
     */
    public static boolean isConanFileExists(Project project) {
        if (project.getBasePath() == null) {
            return false;
        }
        Path conanPyFile = Paths.get(project.getBasePath(), "conanfile.py");
        Path conanTxtFile = Paths.get(project.getBasePath(), "conanfile.txt");
        return conanPyFile.toFile().exists() || conanTxtFile.toFile().exists();
    }

    /**
     * Return true if conan is installed.
     * @param project Intellij project.
     * @return true if conan is installed.
     */
    public static boolean isConanInstalled(Project project){
        IsInstalledCommand isInstalled = new IsInstalledCommand(project);
        isInstalled.run();
        return isInstalled.isInstalled();
    }
}
