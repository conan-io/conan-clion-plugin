package conan.testUtils;

import com.google.common.io.Files;
import conan.profiles.ConanProfile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    public static File createTempDir() {
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        return tempDir;
    }


    public static Set<ConanProfile> createProfilesWithNames(HashSet<String> names) {
        return names.parallelStream().map(ConanProfile::new).collect(Collectors.toSet());
    }

}
