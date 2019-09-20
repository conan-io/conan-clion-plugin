package conan.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.execution.process.ProcessAdapter;
import conan.profiles.ConanProfile;
import conan.testUtils.OpenSSLProjectImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Test
public class ListProfilesTest extends ConanCommandTestBase {

    private static final Set<ConanProfile> TEST_PROFILES = Sets.newHashSet("testProfile1", "testProfile2")
            .stream()
            .map(ConanProfile::new)
            .collect(Collectors.toSet());

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
    public void testListProfiles() {
        this.verifyProfiles(Sets.newHashSet());

        TEST_PROFILES.forEach(newProfile -> {
            ConanCommandBase command = this.mockedComamnd(new ProfileNew(new OpenSSLProjectImpl(), newProfile));
            command.run_async(null, null, new ProcessAdapter(){});
        });

        this.verifyProfiles(TEST_PROFILES);
    }
}
