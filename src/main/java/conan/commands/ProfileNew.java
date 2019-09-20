package conan.commands;

import com.intellij.openapi.project.Project;
import conan.profiles.ConanProfile;

/**
 * Create a new Conan profile.
 * Run "conan profile new <profile-name>"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ProfileNew extends ConanCommandBase {

    public ProfileNew(Project project, ConanProfile conanProfile) {
        super(project, "profile", "new", conanProfile.getName());
    }

}
