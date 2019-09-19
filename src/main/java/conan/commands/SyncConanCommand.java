package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.progress.ProgressManager;
import conan.commands.task.SyncConanTask;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Base class for Sync Conan commands.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class SyncConanCommand implements Runnable {

    private SyncConanTask conanTask;

    public SyncConanCommand(ConanCommandBase conanCommand, @Nullable ProcessListener processListener) {
        conanTask = new SyncConanTask(conanCommand.project, processListener, conanCommand.args);
    }

    @Override
    public void run() {
        // The progress manager is only good for foreground threads.
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(conanTask);
        } else {
            // Run the scan task when the thread is in the foreground.
            SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(conanTask));
        }
    }

    public SyncConanTask getConanTask() {
        return conanTask;
    }
}
