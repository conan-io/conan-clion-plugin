package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;

/**
 * Search Conan packages. Send the results to the input process listener.
 * Run "conan search --raw"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Search extends ConanCommandBase {

    public Search(Project project) {
        super(project, "search", "--raw");
    }

    public void run(ProcessListener processListener) {
        new AsyncConanCommand(this, null, processListener).run();
    }
}
