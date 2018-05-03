package conan.consistency;

import com.intellij.ide.util.PropertiesComponent;

/**
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConsistencyUtils {

    /**
     * Load value from the global properties.
     * @param key the key to search.
     * @return value from the global properties.
     */
    public static String getValue(String key) {
        return PropertiesComponent.getInstance().getValue(key, "");
    }

    /**
     * Save value in the global properties.
     * @param key the key.
     * @param value the value.
     */
    public static void setValue(String key, String value) {
        PropertiesComponent.getInstance().setValue(key, value);
    }

}
