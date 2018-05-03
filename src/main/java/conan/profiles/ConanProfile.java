package conan.profiles;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a Conan profile.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ConanProfile implements Serializable {

    static final long serialVersionUID = 1L;
    private String name;

    // Empty constructor for serialization
    public ConanProfile() {
        this("");
    }

    public ConanProfile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        ConanProfile other = (ConanProfile) o;
        return StringUtils.equals(name, other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
