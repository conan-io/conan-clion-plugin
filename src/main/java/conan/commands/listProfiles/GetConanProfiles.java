package conan.commands.listProfiles;

import com.intellij.openapi.project.Project;
import conan.commands.SyncConanCommand;
import conan.profiles.ConanProfile;

import java.util.List;

/**
 * Get all Conan profiles.
 * Run "conan profile list"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class GetConanProfiles extends SyncConanCommand {

    public GetConanProfiles(List<ConanProfile> profiles, Project project) {
        super(project, new GetConanProfilesProcessListener(profiles), "profile", "list");
    }
}
