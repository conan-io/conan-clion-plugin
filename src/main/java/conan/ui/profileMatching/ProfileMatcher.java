package conan.ui.profileMatching;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import conan.persistency.settings.ConanProjectSettings;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import conan.profiles.ProfileUtils;
import conan.ui.ConanToolWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Set;

/**
 * Configuration dialog to match between CMake and Conan profiles.
 *
 * Created by Yahav Itzhak on Feb 2018.
 */
public class ProfileMatcher extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable profileMatchingTable;
    private Project project;
    private Map<CMakeProfile, ConanProfile> profileMapping;

    private ProfileMatcher(Project project) {
        this.project = project;
        setTitle("Match between CMake and Conan profiles");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        populateProfileMatchingTable(project);
    }

    /**
     * Pop up the profile matching dialog.
     * @param project Intellij project.
     * @param component the configuration button component.
     */
    public static void showDialog(Project project, Component component) {
        ProfileMatcher dialog = new ProfileMatcher(project);
        dialog.setLocationRelativeTo(component);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void onOK() {
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(project);
        conanProjectSettings.setProfileMapping(profileMapping);
        ConanToolWindow conanToolWindow = ServiceManager.getService(project, ConanToolWindow.class);
        conanToolWindow.recreateContent(project);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        createProfileMatchingTable();
        createProfilesMap();
    }

    /**
     * Create an empty profile matching table to show in the ProfileMatcher dialog.
     */
    private void createProfileMatchingTable() {
        profileMatchingTable = new JBTable();
        profileMatchingTable.setShowGrid(false);
        profileMatchingTable.setColumnSelectionAllowed(false);
        profileMatchingTable.setRowSelectionAllowed(false);
        profileMatchingTable.setFocusable(false);
        profileMatchingTable.setDragEnabled(false);
        profileMatchingTable.getTableHeader().setReorderingAllowed(false);
        profileMatchingTable.setDefaultRenderer(ConanProfile.class, new ProfileMatchingCellRenderer());
    }

    /**
     * Create the content of the profile matching table.
     */
    private void createProfilesMap() {
        ConanProjectSettings conanProjectSettings = ConanProjectSettings.getInstance(project);
        profileMapping = Maps.newHashMap(conanProjectSettings.getProfileMapping());
        Set<CMakeProfile> cmakeProfiles = Sets.newHashSet(ProfileUtils.getCmakeProfiles(project));
        cmakeProfiles.forEach(cMakeProfile -> profileMapping.putIfAbsent(cMakeProfile, null));
        profileMapping.keySet().removeIf(cMakeProfile -> !cmakeProfiles.contains(cMakeProfile));
    }

    /**
     * Populate the profile matching table.
     */
    private void populateProfileMatchingTable(Project project) {
        profileMatchingTable.setModel(new ProfileMatchingTableModel(profileMapping));
        profileMatchingTable.setDefaultEditor(ConanProfile.class, new ProfileMatchingCellEditor(profileMapping, project));
    }

}
