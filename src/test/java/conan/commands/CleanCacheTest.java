package conan.commands;

import com.google.common.collect.Sets;
import conan.testUtils.Utils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static conan.testUtils.Consts.OPENSSL_PACKAGES;
import static conan.testUtils.Utils.*;

@Test
public class CleanCacheTest extends ConanCommandTestBase {
    private File tempInstallationDir;

    @BeforeClass
    public void init() {
        tempInstallationDir = Utils.createTempDir();
    }

    @Test
    public void testCleanCache() {
        installPocoProject(tempInstallationDir);
        verifyPackages(OPENSSL_PACKAGES);
        cleanCache();
        verifyPackages(Sets.newHashSet());
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
