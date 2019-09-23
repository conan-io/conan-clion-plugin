package conan.commands.process_adapters;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.openapi.util.Key;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Return Conan Version
 */
public class VersionProcessAdapter extends ProcessAdapter {

    public String conanVersion = null;

    public VersionProcessAdapter() {
    }

    @Override
    public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
        String version = StringUtils.trim(processEvent.getText());

        ProcessOutputType processOutputType = (ProcessOutputType) key;
        if (processOutputType.isStdout() && !StringUtils.isBlank(version)) {
            this.conanVersion = version;
        }
        // TODO: Actual parse of the version string
    }

}
