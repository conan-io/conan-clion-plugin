package conan.commands.listProfiles;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.util.Key;
import conan.profiles.ConanProfile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Populate the list of {@link ConanProfile}.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class GetConanProfilesProcessListener extends ProcessAdapter {

    private static final String NO_PROFILES_STR = "No profiles defined";
    private List<ConanProfile> profiles;

    public GetConanProfilesProcessListener(List<ConanProfile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
        String profile = StringUtils.trim(processEvent.getText());
        ProcessOutputType processOutputType = (ProcessOutputType) key;
        if (isValidConanProfile(processOutputType, profile)) {
            profiles.add(new ConanProfile(profile));
        }
    }

    private boolean isValidConanProfile(ProcessOutputType processOutputType, String conanProfile) {
        if (!processOutputType.isStdout()) {
            // If the output is not stdout then it can't be a valid conan profile
            return false;
        }
        if (StringUtils.isBlank(conanProfile)) {
            // Empty conan profile is not acceptable
            return false;
        }
        if (StringUtils.equals(NO_PROFILES_STR, conanProfile)) {
            // If there are no Conan profiles, "conan list" command prints "No profiles defined"
            return false;
        }
        return true;
    }
}
