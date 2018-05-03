package conan.commands;

import conan.testUtils.Utils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static conan.testUtils.Consts.POCO_PACKAGES;
import static conan.testUtils.Utils.*;

@Test
public class InstallTest extends ConanCommandTestBase {
    private File tempInstallationDir;

    @BeforeClass
    public void init() {
        tempInstallationDir = Utils.createTempDir();
    }

    @Test
    public void testInstall() {
        installPocoProject(tempInstallationDir);
        verifyConanFiles(tempInstallationDir);
        verifyPackages(POCO_PACKAGES);
    }

    @AfterClass
    public void terminate() {
        try {
            FileUtils.deleteDirectory(tempInstallationDir);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
