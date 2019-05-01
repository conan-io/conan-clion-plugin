package conan.commands;

import com.intellij.openapi.project.Project;
import conan.testUtils.OpenSSLProjectImpl;
import conan.testUtils.Utils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;

import static conan.utils.Utils.CONAN_HOME_ENV;

public abstract class ConanCommandTestBase {

    private File conanHomeDir;
    Project project;

    @BeforeClass(alwaysRun = true)
    public void setConanHome() {
        conanHomeDir = Utils.createTempDir();
        System.setProperty(CONAN_HOME_ENV, conanHomeDir.getAbsolutePath());
        project = new OpenSSLProjectImpl();
    }

    @AfterClass(alwaysRun = true)
    public void deleteConanHome() {
        try {
            FileUtils.deleteDirectory(conanHomeDir);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
