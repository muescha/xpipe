package io.xpipe.ext.base.browser;

import io.xpipe.app.browser.FileBrowserEntry;
import io.xpipe.app.browser.OpenFileSystemModel;
import io.xpipe.app.browser.icon.FileType;
import io.xpipe.core.process.ShellControl;

import java.util.List;

public class JarAction extends JavaAction implements FileTypeAction {

    @Override
    public Category getCategory() {
        return Category.CUSTOM;
    }

    @Override
    public boolean isApplicable(OpenFileSystemModel model, List<FileBrowserEntry> entries) {
        return super.isApplicable(model, entries) && FileTypeAction.super.isApplicable(model, entries);
    }

    @Override
    public boolean isApplicable(OpenFileSystemModel model, FileBrowserEntry entry) {
        return entry.getFileName().endsWith(".jar");
    }

    @Override
    protected String createCommand(ShellControl sc, OpenFileSystemModel model, FileBrowserEntry entry) {
        return "java -jar " + entry.getOptionallyQuotedFileName();
    }

    @Override
    public String getName(OpenFileSystemModel model, List<FileBrowserEntry> entries) {
        return "java -jar " + filesArgument(entries);
    }

    @Override
    public FileType getType() {
        return FileType.byId("jar");
    }
}