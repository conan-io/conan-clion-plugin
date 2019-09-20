package conan.commands;

import com.intellij.openapi.project.Project;

/**
 * Clean Conan cache.
 * Run "conan remove * -f"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class RemoveAllForce extends ConanCommandBase {

    public RemoveAllForce(Project project) {
        super(project, "remove", "*", "-f");
    }


}
