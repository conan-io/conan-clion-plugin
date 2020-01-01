package conan.profiles;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a CMake profile.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class CMakeProfile implements Serializable {

    static final long serialVersionUID = 1L;
    private String name;
    private String targetDir;

    // Empty constructor for serialization
    public CMakeProfile() {
    }

    public CMakeProfile(String name, File targetDir) {
        this.name = name;
        this.targetDir = targetDir.getAbsolutePath();
    }

    public CMakeProfile(String name, String targetDir) {
        this.name = name;
        this.targetDir = targetDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        CMakeProfile other = (CMakeProfile) o;
        return StringUtils.equals(name, other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
