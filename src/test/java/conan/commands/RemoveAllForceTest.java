package conan.commands;

import com.google.common.collect.Sets;
import com.intellij.execution.process.ProcessAdapter;
import conan.profiles.CMakeProfile;
import conan.testUtils.ConanPackagesVerifier;
import conan.testUtils.OpenSSLProjectImpl;
import conan.testUtils.Utils;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static conan.testUtils.Consts.DEFAULT_CONAN_PROFILE;
import static conan.testUtils.Consts.OPENSSL_PACKAGES;

@Test
public class RemoveAllForceTest extends ConanCommandTestBase {
    private File tempInstallationDir;

    @BeforeClass
    public void init() {
        tempInstallationDir = Utils.createTempDir();
    }

    @Test
    public void testCleanCache() {
        // Install Poco project
        CMakeProfile cMakeProfile = new CMakeProfile("poco-timer", tempInstallationDir);
        Install installCommand = this.mockedComamnd(new Install(new OpenSSLProjectImpl(), cMakeProfile, DEFAULT_CONAN_PROFILE, false));
        installCommand.run_async(DEFAULT_CONAN_PROFILE, null, new ProcessAdapter(){});

        // Verify packages
        ConanCommandBase conanCommand = this.mockedComamnd(new Search(new OpenSSLProjectImpl()));
        conanCommand.run_async(null, null, new ConanPackagesVerifier(OPENSSL_PACKAGES));

        // Clean cache
        ConanCommandBase removeAllForce = this.mockedComamnd(new RemoveAllForce(new OpenSSLProjectImpl()));
        removeAllForce.run_async(null, null, new ProcessAdapter(){});

        // Verify packages
        ConanCommandBase conanCommand2 = this.mockedComamnd(new Search(new OpenSSLProjectImpl()));
        conanCommand2.run_async(null, null, new ConanPackagesVerifier(Sets.newHashSet()));
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
