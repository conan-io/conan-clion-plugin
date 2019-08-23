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
public class NewProfile extends ConanCommandBase {

    public NewProfile(Project project, ConanProfile conanProfile) {
        super(project, "profile", "new", conanProfile.getName());
    }

    public void run(ProcessListener processListener) {
        new AsyncConanCommand(this, null, processListener).run();
    }
}
