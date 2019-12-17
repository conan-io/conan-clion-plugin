package conan.persistency.settings;

import com.google.common.collect.Maps;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Project Conan settings. Contains the CMakeProfile -> ConanProfile mapping.
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
@State(name = "ConanProjectSettings", storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)})
public class ConanProjectSettings implements PersistentStateComponent<ConanProjectSettings> {

    private Map<CMakeProfile, ConanProfile> profileMapping = Maps.newHashMap();
    private String conanPath;
    private boolean installUpdate;
    private String installBuildPolicy;
    private String installArgs;
    public static List<String> buildPolicies = Arrays.asList("missing", "always", "cascade", "outdated", "never");

    public static ConanProjectSettings getInstance(Project project) {
        return ServiceManager.getService(project, ConanProjectSettings.class);
    }

    @Override
    public ConanProjectSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConanProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setProfileMapping(Map<CMakeProfile, ConanProfile> profileMapping) {
        this.profileMapping = profileMapping;
    }

    public Map<CMakeProfile, ConanProfile> getProfileMapping() {
        return this.profileMapping;
    }

    public String getInstallArgs() {
        String installArgsCommand = "--build";
        if (installBuildPolicy != "always") {
            installArgsCommand += "=" + installBuildPolicy;
        }
        if (installUpdate) {
            installArgsCommand += " --update";
        }
        if (!installArgs.isEmpty()) {
            installArgsCommand += " " + installArgs;
        }
        return installArgsCommand;
    }

    /* Getters and setters for Config window */
    public void setConfigInstallArgs(String configInstallArgs) {
        this.installArgs = configInstallArgs;
    }

    public String getConfigInstallArgs() {
        return installArgs == null ? "" : installArgs;
    }

    public void setConanPath(String conanPath) {
        this.conanPath = conanPath;
    }

    public String getConanPath() {
        return conanPath;
    }

    public boolean getInstallUpdate() {
        return installUpdate;
    }

    public void setInstallUpdate(boolean value) {
        this.installUpdate = value;
    }

    public boolean setInstallBuildPolicy(String value) {
        if (buildPolicies.contains(value)) {
            this.installBuildPolicy = value;
            return true;
        }
        return false;
    }

    public String getInstallBuildPolicy() {
        return installBuildPolicy;
    }


}
