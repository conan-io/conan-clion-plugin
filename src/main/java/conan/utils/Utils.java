package conan.utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.Nullable;

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
}
