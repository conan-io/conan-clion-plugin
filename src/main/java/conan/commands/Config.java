package conan.commands;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.project.Project;

/**
 * Used to initialize conan.
 * Run "conan config"
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Config extends SyncConanCommand {
    public Config(Project p) {
        super(p, new ProcessAdapter(){}, "config");
    }
}
