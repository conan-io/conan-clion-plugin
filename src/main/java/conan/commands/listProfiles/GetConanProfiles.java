package conan.commands.listProfiles;

import com.intellij.openapi.project.Project;
import conan.commands.ConanCommandBase;
import conan.commands.SyncConanCommand;
import conan.profiles.ConanProfile;

import java.util.List;

/**
 * Get all Conan profiles.
 * Run "conan profile list"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class GetConanProfiles extends ConanCommandBase {

    public GetConanProfiles(Project project) {
        super(project, "profile", "list");
    }

    public void run(List<ConanProfile> profiles) {
        new SyncConanCommand(this, new GetConanProfilesProcessListener(profiles)).run();
    }
}
