package conan.commands;

import conan.testUtils.Utils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;

import static conan.utils.Utils.CONAN_HOME_ENV;

public abstract class ConanCommandTestBase {

    private File conanHomeDir;

    @BeforeClass
    public void init() {
        conanHomeDir = Utils.createTempDir();
        System.setProperty(CONAN_HOME_ENV, conanHomeDir.getAbsolutePath());
    }

    @AfterClass
    public void terminate() {
        try {
            FileUtils.deleteDirectory(conanHomeDir);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
