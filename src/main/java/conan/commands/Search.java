package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.project.Project;

/**
 * Search Conan packages. Send the results to the input process listener.
 * Run "conan search --raw"
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class Search extends AsyncConanCommand {

    public Search(Project project, ProcessListener processListener) {
        super(project, null, processListener, "search", "--raw");
    }
}
