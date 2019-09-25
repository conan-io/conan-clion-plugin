package conan.testUtils;

import com.google.common.collect.Sets;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.util.Key;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;

import java.util.Set;

public class ConanPackagesVerifier extends ProcessAdapter {

    private final Set<String> expectedPackages;
    private Set<String> conanPackages = Sets.newHashSet();

    public ConanPackagesVerifier(Set<String> expectedPackages) {
        this.expectedPackages = expectedPackages;
    }

    @Override
    public void processTerminated(@NotNull ProcessEvent processEvent) {
        Assert.assertEquals(conanPackages, expectedPackages);
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
        String conanPackage = StringUtils.trim(processEvent.getText());
        String packageName = conanPackage.split("/")[0];
        ProcessOutputType processOutputType = (ProcessOutputType) key;
        if (processOutputType.isStdout() && StringUtils.isNotBlank(conanPackage)) {
            conanPackages.add(packageName);
        }
    }
}