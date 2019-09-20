package conan.testUtils;

import com.google.common.collect.Sets;
import conan.profiles.ConanProfile;

import java.util.Set;

public class Consts {
    public static final ConanProfile DEFAULT_CONAN_PROFILE = new ConanProfile("default");
    public static final Set<String> CONAN_INSTALL_FILES = Sets.newHashSet("conanbuildinfo.cmake", "conanbuildinfo.txt", "conaninfo.txt");
    public static final Set<String> OPENSSL_PACKAGES = Sets.newHashSet("OpenSSL", "zlib");
    public static final String BINCRAFTERS_URL = "https://github.com/bincrafters/conan-config.git";
    public static final Set<ConanProfile> BINCRAFTERS_PROFILES = Utils.createProfilesWithNames(Sets.newHashSet("linux-gcc49-amd64", "linux-clang39-amd64", "windows-msvc12-amd64", "android-clang7-i386", "android-clang8-amd64", "linux-gcc54-amd64", "orangepi", "freebsd-clang34-amd64", "linux-gcc48-amd64", "linux-gcc63-amd64", "linux-clang40-amd64", "android-clang7-amd64", "android-clang7-armv7", "android-clang7-armv8", "android-clang8-i386", "windows-msvc14-amd64", "windows-msvc15-amd64", "android-clang8-armv7", "android-clang8-armv8", "linux-gcc41-amd64"));
    public static final Set<ConanProfile> LOCAL_PROFILES = Utils.createProfilesWithNames(Sets.newHashSet("test_profile"));
}
