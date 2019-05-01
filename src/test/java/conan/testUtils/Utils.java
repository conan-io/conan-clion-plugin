package conan.testUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.progress.DumbProgressIndicator;
import com.intellij.openapi.project.Project;
import conan.commands.*;
import conan.commands.listProfiles.GetConanProfiles;
import conan.commands.task.AsyncConanTask;
import conan.commands.task.SyncConanTask;
import conan.profiles.CMakeProfile;
import conan.profiles.ConanProfile;
import org.testng.Assert;

import java.io.File;
import java.util.List;
import java.util.Set;

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
        AsyncConanCommand install = new Install(new OpenSSLProjectImpl(), new ProcessAdapter(){}, cMakeProfile, DEFAULT_CONAN_PROFILE, false);
        install.addParameter("--build=missing");
        Utils.runConanCommand(install);
    }

    public static void verifyPackages(Set<String> expectedPackages) {
        AsyncConanCommand search = new Search(new OpenSSLProjectImpl(), new ConanPackagesVerifier(expectedPackages));
        Utils.runConanCommand(search);
    }

    public static void cleanCache() {
        AsyncConanCommand cleanCache = new CleanCache(new OpenSSLProjectImpl(), new ProcessAdapter(){});
        Utils.runConanCommand(cleanCache);
    }

    public static void verifyProfiles(Set<ConanProfile> expectedProfiles, Project project) {
        List<ConanProfile> conanProfiles = Lists.newArrayList();
        Utils.runConanCommand(new Config(project)); // Prevents "Remotes registry file missing" message
        Utils.runConanCommand(new GetConanProfiles(conanProfiles, project));
        Assert.assertEquals(Sets.newHashSet(conanProfiles), expectedProfiles);
    }

    public static void configInstall() {
        AsyncConanCommand configInstall = new ConfigInstall(new OpenSSLProjectImpl(), new ProcessAdapter(){}, BINCRAFTERS_URL);
        Utils.runConanCommand(configInstall);
    }

    public static void createProfiles(Set<ConanProfile> newProfiles) {
        newProfiles.forEach(newProfile -> {
            AsyncConanCommand newProfileCommand = new NewProfile(new OpenSSLProjectImpl(), new ProcessAdapter(){}, newProfile);
            Utils.runConanCommand(newProfileCommand);
        });
    }

    public static void verifyConanFiles(File installationDir) {
        String[] tmpDirFiles = installationDir.list();
        Assert.assertNotNull(tmpDirFiles);
        Set<String> files = Sets.newHashSet(tmpDirFiles);
        CONAN_INSTALL_FILES.forEach(file -> Assert.assertTrue(files.contains(file), file + " is missing, "));
    }
}
