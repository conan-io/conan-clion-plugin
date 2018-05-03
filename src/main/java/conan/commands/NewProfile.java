package conan.commands;

import com.intellij.execution.process.ProcessListener;
import conan.profiles.ConanProfile;

/**
 * Create a new Conan profile.
 * Run "conan profile new <profile-name>"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class NewProfile extends AsyncConanCommand {

    public NewProfile(ProcessListener processListener, ConanProfile conanProfile) {
        super(null, "", null, processListener, "profile", "new", conanProfile.getName());
    }
}
