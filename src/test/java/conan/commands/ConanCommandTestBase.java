package conan.commands;

import com.intellij.openapi.progress.DumbProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import conan.testUtils.OpenSSLProjectImpl;
import conan.testUtils.Utils;
import org.apache.commons.io.FileUtils;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;

import static conan.utils.Utils.CONAN_HOME_ENV;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

public abstract class ConanCommandTestBase {

    private File conanHomeDir;
    Project project;

    @BeforeMethod(alwaysRun = true)
    public void setConanHome() {
        conanHomeDir = Utils.createTempDir();
        System.setProperty(CONAN_HOME_ENV, conanHomeDir.getAbsolutePath());
        project = new OpenSSLProjectImpl();
    }

    @AfterMethod(alwaysRun = true)
    public void deleteConanHome() {
        try {
            FileUtils.deleteDirectory(conanHomeDir);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    protected  <T extends ConanCommandBase>
    T mockedComamnd(T command) {
        T spy = spy(command);
        doAnswer((Answer) invocation -> {
            Task task = invocation.getArgument(0);
            task.run(new DumbProgressIndicator());
            return null;
        }).when(spy).run(any(Task.class));
        return spy;
    }
}
