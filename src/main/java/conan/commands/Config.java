package conan.commands;

import com.intellij.openapi.project.Project;

/**
 * Used to initialize conan.
 * Run "conan config"
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Config extends ConanCommandBase {
    public Config(Project project) {
        super(project, "config");
    }
}
