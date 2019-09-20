package conan.testUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.progress.DumbProgressIndicator;
import com.intellij.openapi.project.Project;
import conan.commands.*;
import conan.commands.ProfileList;
import conan.commands.process_adapters.ProfileListProcessAdapter;
import conan.commands.task.AsyncConanTask;
import conan.commands.task.SyncConanTask;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.testng.Assert;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static conan.testUtils.Consts.*;

public class Utils {

    public static File createTempDir() {
        File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        return tempDir;
    }

    private static void runConanCommand(AsyncConanCommand command) {
        AsyncConanTask asyncConanTask = command.getConanTask();
        asyncConanTask.run(new DumbProgressIndicator());
    }

    private static void runConanCommand(SyncConanCommand command) {
        SyncConanTask syncConanTask = command.getConanTask();
        syncConanTask.run(new DumbProgressIndicator());
    }

    public static void installPocoProject(File installationDir) {
        CMakeProfile cMakeProfile = new CMakeProfile("poco-timer", installationDir);
        Install installCommand = new Install(new OpenSSLProjectImpl(), cMakeProfile, DEFAULT_CONAN_PROFILE, false);
        installCommand.addParameter("--build=missing");
        AsyncConanCommand install = new AsyncConanCommand(installCommand, DEFAULT_CONAN_PROFILE, new ProcessAdapter(){});
        Utils.runConanCommand(install);
    }

    public static void verifyPackages(Set<String> expectedPackages) {
        ConanCommandBase conanCommand = new Search(new OpenSSLProjectImpl());
        AsyncConanCommand search = new AsyncConanCommand(conanCommand, null, new ConanPackagesVerifier(expectedPackages));
        Utils.runConanCommand(search);
    }

    public static void cleanCache() {
        ConanCommandBase conanCommandBase = new RemoveAllForce(new OpenSSLProjectImpl());
        AsyncConanCommand cleanCache = new AsyncConanCommand(conanCommandBase, null, new ProcessAdapter(){});
        Utils.runConanCommand(cleanCache);
    }

    public static void verifyProfiles(Set<ConanProfile> expectedProfiles, Project project) {
        List<ConanProfile> conanProfiles = Lists.newArrayList();

        // Prevents "Remotes registry file missing" message
        ConanCommandBase config = new Config(project);
        SyncConanCommand syncCommand = new SyncConanCommand(config, new ProcessAdapter(){});
        Utils.runConanCommand(syncCommand);

        ConanCommandBase getConanProfiles = new ProfileList(project);
        SyncConanCommand syncConanCommand = new SyncConanCommand(getConanProfiles, new ProfileListProcessAdapter(conanProfiles));
        Utils.runConanCommand(syncConanCommand);

        Assert.assertEquals(Sets.newHashSet(conanProfiles), expectedProfiles);
    }

    public static void configInstall(Project project, String source) {
        ConanCommandBase configInstall = new ConfigInstall(new OpenSSLProjectImpl(), source);
        AsyncConanCommand asyncConanCommand = new AsyncConanCommand(configInstall, null, new ProcessAdapter(){});
        Utils.runConanCommand(asyncConanCommand);
    }

    public static void createProfiles(Set<ConanProfile> newProfiles) {
        newProfiles.forEach(newProfile -> {
            ConanCommandBase newProfileCommand = new NewProfile(new OpenSSLProjectImpl(), newProfile);
            AsyncConanCommand asyncConanCommand = new AsyncConanCommand(newProfileCommand, null, new ProcessAdapter(){});
            Utils.runConanCommand(asyncConanCommand);
        });
    }

    public static Set<ConanProfile> createProfilesWithNames(HashSet<String> names) {
        return names.parallelStream().map(ConanProfile::new).collect(Collectors.toSet());
    }

    public static void verifyConanFiles(File installationDir) {
        String[] tmpDirFiles = installationDir.list();
        Assert.assertNotNull(tmpDirFiles);
        Set<String> files = Sets.newHashSet(tmpDirFiles);
        CONAN_INSTALL_FILES.forEach(file -> Assert.assertTrue(files.contains(file), file + " is missing, "));
    }
}
