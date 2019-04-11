package conan.commands;

import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Objects;

import static conan.testUtils.Consts.*;
import static conan.testUtils.Utils.*;

@Test
public class ConfigInstallTest extends ConanCommandTestBase {

    @Test
    public void testConfigInstall() {
        verifyProfiles(Sets.newHashSet(), project);
        configInstall(project, BINCRAFTERS_URL);
        verifyProfiles(BINCRAFTERS_PROFILES, project);
    }

    @Test
    public void testConfigInstallLocalDir() {
        verifyProfiles(Sets.newHashSet(), project);
        configInstall(project, "test_config");
        verifyProfiles(LOCAL_PROFILES, project);
    }

    @Test
    public void testConfigInstallAbsoluteDir() {
        verifyProfiles(Sets.newHashSet(), project);

        String project_path = Objects.requireNonNull(project.getBasePath());
        String config_path = Paths.get(project_path, "test_config").toAbsolutePath().toString();
        configInstall(project, config_path);

        verifyProfiles(LOCAL_PROFILES, project);
    }
}
