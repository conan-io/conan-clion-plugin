package conan.ui.profileMatching;

import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Show the selected Conan profile in the {@link ProfileMatcher}.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
class ProfileMatchingCellRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ComboBox<Object> comboBox = new ComboBox<>();
        if (value != null) {
            comboBox.addItem(value);
            comboBox.setSelectedItem(value);
        }
        return comboBox;
    }
}
