package conan.persistency.settings;

import com.google.common.collect.Maps;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Project Conan settings. Contains the CMakeProfile -> ConanProfile mapping.
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
@State(name = "ConanProjectSettings", storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)})
public class ConanProjectSettings implements PersistentStateComponent<ConanProjectSettings> {

    private Map<CMakeProfile, ConanProfile> profileMapping = Maps.newHashMap();
    private String installArgs;
    private String conanPath;

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
        return installArgs;
    }

    public String getConanPath() {
        return conanPath;
    }

    public void setInstallArgs(String installArgs) {
        this.installArgs = installArgs;
    }

    public void setConanPath(String conanPath) {
        this.conanPath = conanPath;
    }
}
