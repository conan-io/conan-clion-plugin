package conan.ui.profileMatching;

import com.intellij.openapi.ui.ComboBox;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import conan.profiles.ProfileMatchingPair;
import conan.profiles.ProfileUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * Create the Conan profiles list in the {@link ProfileMatcher}
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
class ProfileMatchingCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private ProfileMatchingPair profileMatchingPair;
    private Map<CMakeProfile, ConanProfile> profileMapping;
    private List<ConanProfile> conanProfiles;

    ProfileMatchingCellEditor(Map<CMakeProfile, ConanProfile> profileMapping) {
        this.profileMapping = profileMapping;
        this.conanProfiles = ProfileUtils.getConanProfiles();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ComboBox<ConanProfile> conanProfileJComboBox = (ComboBox<ConanProfile>) actionEvent.getSource();
        profileMatchingPair.setConanProfile((ConanProfile) conanProfileJComboBox.getSelectedItem());
        profileMapping.put(profileMatchingPair.getCMakeProfile(), profileMatchingPair.getConanProfile());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        ComboBox<ConanProfile> conanProfilesComboBox = new ComboBox<>();
        conanProfilesComboBox.addItem(new ConanProfile());
        for (ConanProfile conanProfile : conanProfiles) {
            conanProfilesComboBox.addItem(conanProfile);
        }
        profileMatchingPair = new ProfileMatchingPair((CMakeProfile) table.getValueAt(row, 0), (ConanProfile) value);
        conanProfilesComboBox.addActionListener(this);
        return conanProfilesComboBox;
    }

    @Override
    public Object getCellEditorValue() {
        return this.profileMatchingPair.getConanProfile();
    }
}
