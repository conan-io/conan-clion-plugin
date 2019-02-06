package conan.commands;

import com.intellij.execution.process.ProcessAdapter;

/**
 * Used to initialize conan.
 * Run "conan config"
 * <p>
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Config extends SyncConanCommand {
    public Config() {
        super(null, new ProcessAdapter(){}, "config");
    }
}
