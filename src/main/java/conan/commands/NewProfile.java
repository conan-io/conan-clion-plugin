package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;
import conan.profiles.ConanProfile;

/**
 * Create a new Conan profile.
 * Run "conan profile new <profile-name>"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class NewProfile extends AsyncConanCommand {

    public NewProfile(Project project, ProcessListener processListener, ConanProfile conanProfile) {
        super(project, null, processListener, "profile", "new", conanProfile.getName());
    }
}
