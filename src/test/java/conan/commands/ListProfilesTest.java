package conan.commands;

import com.google.common.collect.Sets;
import conan.profiles.ConanProfile;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static conan.testUtils.Utils.createProfiles;
import static conan.testUtils.Utils.verifyProfiles;

@Test
public class ListProfilesTest extends ConanCommandTestBase {

    private static final Set<ConanProfile> TEST_PROFILES = Sets.newHashSet("testProfile1", "testProfile2")
            .stream()
            .map(ConanProfile::new)
            .collect(Collectors.toSet());

    @Test
    public void testListProfiles() {
        //verifyProfiles(Sets.newHashSet());
        createProfiles(TEST_PROFILES);
        //verifyProfiles(TEST_PROFILES);
    }
}
