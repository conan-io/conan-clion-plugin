package conan.commands;

import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import static conan.testUtils.Consts.*;
import static conan.testUtils.Utils.*;

@Test
public class ConfigInstallTest extends ConanCommandTestBase {

    @Test
    public void testConfigInstall() {
        verifyProfiles(Sets.newHashSet(), project );
        configInstall(BINCRAFTERS_URL);
        verifyProfiles(BINCRAFTERS_PROFILES, project);
    }
}
