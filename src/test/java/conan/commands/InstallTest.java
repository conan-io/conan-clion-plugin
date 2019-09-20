package conan.commands;

import com.google.common.collect.Sets;
import com.intellij.execution.process.ProcessAdapter;
import conan.profiles.CMakeProfile;
import conan.testUtils.ConanPackagesVerifier;
import conan.testUtils.OpenSSLProjectImpl;
import conan.testUtils.Utils;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static conan.testUtils.Consts.*;

@Test
public class InstallTest extends ConanCommandTestBase {
    private File tempInstallationDir;

    @BeforeClass
    public void init() {
        tempInstallationDir = Utils.createTempDir();
    }

    @Test
    public void testInstall() {
        // Install Poco project
        CMakeProfile cMakeProfile = new CMakeProfile("poco-timer", tempInstallationDir);
        Install installCommand = this.mockedComamnd(new Install(new OpenSSLProjectImpl(), cMakeProfile, DEFAULT_CONAN_PROFILE, false));
        installCommand.run_async(DEFAULT_CONAN_PROFILE, null, new ProcessAdapter(){});

        // Verify ConanFiles
        String[] tmpDirFiles = tempInstallationDir.list();
        Assert.assertNotNull(tmpDirFiles);
        Set<String> files = Sets.newHashSet(tmpDirFiles);
        CONAN_INSTALL_FILES.forEach(file -> Assert.assertTrue(files.contains(file), file + " is missing, "));

        // Verify packages
        ConanCommandBase conanCommand = this.mockedComamnd(new Search(new OpenSSLProjectImpl()));
        conanCommand.run_async(null, null, new ConanPackagesVerifier(OPENSSL_PACKAGES));
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
