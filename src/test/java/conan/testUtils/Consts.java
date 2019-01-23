package conan.testUtils;

import com.google.common.collect.Sets;
import conan.profiles.ConanProfile;

import java.util.Set;
import java.util.stream.Collectors;

public class Consts {
    static final ConanProfile DEFAULT_CONAN_PROFILE = new ConanProfile("default");
    static final Set<String> CONAN_INSTALL_FILES = Sets.newHashSet("conanbuildinfo.cmake", "conanbuildinfo.txt", "conaninfo.txt");
    public static final Set<String> OPENSSL_PACKAGES = Sets.newHashSet("OpenSSL", "zlib");
    static final String BINCRAFTERS_URL = "https://github.com/bincrafters/conan-config.git";
    public static final Set<ConanProfile> BINCRAFTERS_PROFILES = Sets.newHashSet("freebsd-clang34-amd64", "linux-clang39-amd64",
            "linux-clang40-amd64", "linux-gcc41-amd64", "linux-gcc48-amd64", "linux-gcc49-amd64", "linux-gcc54-amd64",
            "linux-gcc63-amd64", "orangepi", "windows-msvc12-amd64", "windows-msvc14-amd64", "windows-msvc15-amd64")
            .parallelStream()
            .map(ConanProfile::new)
            .collect(Collectors.toSet());
}
