package conan.commands;

import conan.testUtils.OpenSSLProjectImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IsInstalledTest extends ConanCommandTestBase {

    @Test
    public void testIsInstalled() {
        Version version = this.mockedComamnd(new Version(new OpenSSLProjectImpl()));
        String vString = "";
        version.run_sync(vString);
        //Assert.assertTrue(IsInstalledCommand.isInstalled(new OpenSSLProjectImpl()));
    }
}
