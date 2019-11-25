package com.wembleyleach.filehandle;

import java.util.List;
import java.util.stream.Collectors;

public class ReplaceTextLivePreview implements LivePreviewBehavior {

    private final FileHandleController fileHandleController;

    ReplaceTextLivePreview(FileHandleController fileHandleController) {
        this.fileHandleController = fileHandleController;
    }

    @Override
    public void livePreview() {
        if(fileHandleController.getFiles() == null || fileHandleController.getFiles().size() == 0 || fileHandleController.getBaseNames() == null || fileHandleController.getBaseNames().getItems().size() == 0) {
            fileHandleController.showAlertWithMessage("You haven't added any files to rename yet.");
            return;
        }

        fileHandleController.getNewBaseNames().getItems().setAll(fileHandleController.getBaseNames().getItems());

        List<String> renamedFiles = fileHandleController.getNewBaseNames().getItems().stream()
                .filter(fileName -> fileName.contains(fileHandleController.getFindText().getText()))
                .map(fileName -> fileName.replaceAll(fileHandleController.getFindText().getText(), fileHandleController.getReplaceText().getText()))
                .collect(Collectors.toList());

        if(renamedFiles.size() > 0)
            fileHandleController.getNewBaseNames().getItems().setAll(renamedFiles);
    }
}
