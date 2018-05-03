package conan.commands;

import conan.testUtils.PocoProjectImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IsInstalledTest extends ConanCommandTestBase {

    @Test
    public void testIsInstalled() {
        IsInstalledCommand isInstalled = new IsInstalledCommand(new PocoProjectImpl());
        isInstalled.run();
        Assert.assertTrue(isInstalled.isInstalled());
    }
}
