package com.wembleyleach.filehandle;

public class FormatTextLivePreview implements LivePreviewBehavior {

    private final FileHandleController fileHandleController;

    public FormatTextLivePreview(FileHandleController fileHandleController) {
        this.fileHandleController = fileHandleController;
    }

    @Override
    public void livePreview() {
        System.out.println("Performing format live preview.");
        if(fileHandleController.getFiles() == null || fileHandleController.getFiles().size() == 0 || fileHandleController.getBaseNames() == null || fileHandleController.getBaseNames().getItems().size() == 0) {
            fileHandleController.showAlertWithMessage("You haven't added any files to rename yet.");
            return;
        } else if (fileHandleController.getCurrentNameFormatOption() == null) {
            fileHandleController.showAlertWithMessage("You haven't chosen a format for your new custom file name.");
            return;
        } else if(fileHandleController.getCurrentNameFormatLocationOption() == null) {
            fileHandleController.showAlertWithMessage("You haven't chosen the location to add your new text yet.");
            return;
        }
    }

}

