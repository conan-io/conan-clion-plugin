package conan.commands;

import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.cpp.cmake.CMakeRunner;
import conan.commands.task.AsyncConanTask;
import conan.profiles.ConanProfile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Base class for Async Conan commands.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class AsyncConanCommand implements Runnable {

    private AsyncConanTask conanTask;

    AsyncConanCommand(ConanCommandBase conanCommand, ConanProfile conanProfile) {
        this(conanCommand, conanProfile, null, null);
    }

    AsyncConanCommand(ConanCommandBase conanCommand, ConanProfile conanProfile, CMakeRunner.Listener cmakeListener) {
        this(conanCommand, conanProfile, cmakeListener, null);
    }

    public AsyncConanCommand(ConanCommandBase conanCommand, ConanProfile conanProfile, ProcessListener processListener) {
        this(conanCommand, conanProfile, null, processListener);
    }

    private AsyncConanCommand(ConanCommandBase conanCommand, ConanProfile conanProfile, @Nullable CMakeRunner.Listener cmakeListener, @Nullable ProcessListener processListener) {
        this.conanTask = new AsyncConanTask(conanCommand.project, conanProfile, cmakeListener, processListener, conanCommand.args);
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

    public AsyncConanTask getConanTask() {
        return conanTask;
    }
}
