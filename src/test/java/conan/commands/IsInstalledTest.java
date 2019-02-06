package conan.commands;

import conan.testUtils.OpenSSLProjectImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IsInstalledTest extends ConanCommandTestBase {

    @Test
    public void testIsInstalled() {
        IsInstalledCommand isInstalled = new IsInstalledCommand(new OpenSSLProjectImpl());
        isInstalled.run();
        Assert.assertTrue(isInstalled.isInstalled());
    }
}
