package conan.profiles;

/**
 * Pair of {@link CMakeProfile} and {@link ConanProfile}
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ProfileMatchingPair {
    private CMakeProfile cmakeProfile;
    private ConanProfile conanProfile;

    public ProfileMatchingPair(CMakeProfile cmakeProfile, ConanProfile conanProfile) {
        this.cmakeProfile = cmakeProfile;
        this.conanProfile = conanProfile;
    }

    public CMakeProfile getCMakeProfile() {
        return cmakeProfile;
    }

    public void setCMakeProfile(CMakeProfile cmakeProfile) {
        this.cmakeProfile = cmakeProfile;
    }

    public ConanProfile getConanProfile() {
        return conanProfile;
    }

    public void setConanProfile(ConanProfile conanProfile) {
        this.conanProfile = conanProfile;
    }
}
