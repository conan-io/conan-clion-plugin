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
public abstract class AsyncConanCommand extends ConanCommandBase {

    private AsyncConanTask conanTask;

    AsyncConanCommand(@Nullable Project project, String message, String... args) {
        this(project, message, null, null, null, args);
    }

    AsyncConanCommand(@Nullable Project project, String message, ConanProfile conanProfile, CMakeRunner.Listener cmakeListener, String... args) {
        this(project, message, conanProfile, cmakeListener, null, args);
    }

    AsyncConanCommand(@Nullable Project project, String message, ConanProfile conanProfile, ProcessListener processListener, String... args) {
        this(project, message, conanProfile, null, processListener, args);
    }

    private AsyncConanCommand(@Nullable Project project, String message, ConanProfile conanProfile, @Nullable CMakeRunner.Listener cmakeListener, @Nullable ProcessListener processListener, String... args) {
        super(project, args);
        this.conanTask = new AsyncConanTask(project, message, conanProfile, cmakeListener, processListener, super.args);
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
