package io.xpipe.ext.base.browser;

import io.xpipe.app.browser.FileBrowserEntry;
import io.xpipe.app.browser.OpenFileSystemModel;
import io.xpipe.app.browser.action.ApplicationPathAction;
import io.xpipe.app.browser.action.MultiExecuteAction;

import java.util.List;

public abstract class JavaAction extends MultiExecuteAction implements ApplicationPathAction {

    @Override
    public String getName(OpenFileSystemModel model, List<FileBrowserEntry> entries) {
        return "Java";
    }

    @Override
    public String getExecutable() {
        return "java";
    }
}