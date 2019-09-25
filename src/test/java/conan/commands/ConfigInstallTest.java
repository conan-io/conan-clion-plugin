package conan.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.execution.process.ProcessAdapter;
import conan.profiles.ConanProfile;
import conan.testUtils.OpenSSLProjectImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static conan.testUtils.Consts.*;


@Test
public class ConfigInstallTest extends ConanCommandTestBase {

    private void verifyProfiles(Set<ConanProfile> expectedProfiles) {
        List<ConanProfile> conanProfiles = Lists.newArrayList();

        // Prevents "Remotes registry file missing" message
        ConanCommandBase config = mockedComamnd(new Config(project));
        config.run_sync(new ProcessAdapter() {});

        ProfileList profileList = mockedComamnd(new ProfileList(project));
        profileList.run_sync(conanProfiles);

        Assert.assertEquals(Sets.newHashSet(conanProfiles), expectedProfiles);
    }

    @Test
    public void testConfigInstall() {
        this.verifyProfiles(Sets.newHashSet());

        ConfigInstall configInstall = mockedComamnd(new ConfigInstall(new OpenSSLProjectImpl(), BINCRAFTERS_URL));
        configInstall.run_async(null, null, new ProcessAdapter(){});

        this.verifyProfiles(BINCRAFTERS_PROFILES);
    }

    @Test
    public void testConfigInstallLocalDir() {
        this.verifyProfiles(Sets.newHashSet());

        ConfigInstall configInstall = mockedComamnd(new ConfigInstall(project, "test_config"));
        configInstall.run_async(null, null, new ProcessAdapter(){});

        this.verifyProfiles(LOCAL_PROFILES);
    }

    @Test
    public void testConfigInstallAbsoluteDir() {
        this.verifyProfiles(Sets.newHashSet());

        String project_path = Objects.requireNonNull(project.getBasePath());
        String config_path = Paths.get(project_path, "test_config").toAbsolutePath().toString();
        ConfigInstall configInstall = mockedComamnd(new ConfigInstall(project, config_path));
        configInstall.run_async(null, null, new ProcessAdapter(){});

        this.verifyProfiles(LOCAL_PROFILES);
    }
}
