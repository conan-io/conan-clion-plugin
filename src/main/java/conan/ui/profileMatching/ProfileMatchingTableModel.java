package conan.ui.profileMatching;

import com.google.common.collect.Lists;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import conan.profiles.ProfileMatchingPair;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Map;

/**
 * Create the Profile matching in {@link ProfileMatcher}.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
class ProfileMatchingTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"CMake Profile", "Conan Profile"};
    private List<ProfileMatchingPair> profileMatchingPairs = Lists.newArrayList();

    ProfileMatchingTableModel(Map<CMakeProfile, ConanProfile> profileMapping) {
        profileMapping.forEach((cMakeProfile, conanProfile) -> profileMatchingPairs.add(new ProfileMatchingPair(cMakeProfile, conanProfile)));
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ProfileMatchingPair profileMatchingPair = profileMatchingPairs.get(rowIndex);
        if (columnIndex == 0) {
            profileMatchingPair.setCMakeProfile((CMakeProfile) value);
            return;
        }
        profileMatchingPair.setConanProfile((ConanProfile) value);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProfileMatchingPair profileMatchingPair = profileMatchingPairs.get(rowIndex);
        if (columnIndex == 0) {
            return profileMatchingPair.getCMakeProfile();
        }
        return profileMatchingPair.getConanProfile();
    }

    @Override
    public int getRowCount() {
        return profileMatchingPairs.size();
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Class getColumnClass(int column) {
        if (column == 0) {
            return String.class;
        }
        return ConanProfile.class;
    }

}
