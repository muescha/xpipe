package io.xpipe.app.browser;

import io.xpipe.app.browser.icon.DirectoryType;
import io.xpipe.app.browser.icon.FileType;
import io.xpipe.core.impl.FileNames;
import io.xpipe.core.store.FileSystem;
import lombok.Getter;

@Getter
public class FileBrowserEntry {

    private final FileListModel model;
    private final FileSystem.FileEntry rawFileEntry;
    private final boolean synthetic;
    private final FileType fileType;
    private final DirectoryType directoryType;

    public FileBrowserEntry(FileSystem.FileEntry rawFileEntry, FileListModel model, boolean synthetic) {
        this.rawFileEntry = rawFileEntry;
        this.model = model;
        this.synthetic = synthetic;
        this.fileType = fileType(rawFileEntry);
        this.directoryType = directoryType(rawFileEntry);
    }

    private static FileType fileType(FileSystem.FileEntry rawFileEntry) {
        if (rawFileEntry.isDirectory()) {
            return null;
        }

        for (var f : FileType.ALL) {
            if (f.matches(rawFileEntry)) {
                return f;
            }
        }

        return null;
    }

    private static DirectoryType directoryType(FileSystem.FileEntry rawFileEntry) {
        if (!rawFileEntry.isDirectory()) {
            return null;
        }

        for (var f : DirectoryType.ALL) {
            if (f.matches(rawFileEntry)) {
                return f;
            }
        }

        return null;
    }

    public String getFileName() {
        return FileNames.getFileName(getRawFileEntry().getPath());
    }

    public String getOptionallyQuotedFileName() {
        var n = getFileName();
        return FileNames.quoteIfNecessary(n);
    }

    public String getOptionallyQuotedFilePath() {
        var n = rawFileEntry.getPath();
        return FileNames.quoteIfNecessary(n);
    }
}
