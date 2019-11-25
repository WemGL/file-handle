package com.wembleyleach.filehandle;

import com.wembleyleach.filehandle.enums.NameLocation;
import javafx.event.ActionEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class AddTextLivePreview implements LivePreviewBehavior {

    private static final String LAST_PERIOD_LOOKAHEAD_REGEX = "\\.(?=[^\\.]+$)";
    private final FileHandleController fileHandleController;

    AddTextLivePreview(FileHandleController fileHandleController) {
        this.fileHandleController = fileHandleController;
    }

    @Override
    public void livePreview() {
        if(fileHandleController.getFiles() == null || fileHandleController.getFiles().size() == 0 || fileHandleController.getBaseNames() == null || fileHandleController.getBaseNames().getItems().size() == 0) {
            fileHandleController.showAlertWithMessage("You haven't added any files to rename yet.");
            return;
        } else if(fileHandleController.getCurrentAddTextNameLocationOption() == null) {
            fileHandleController.showAlertWithMessage("You haven't chosen the location to add your new text yet.");
            return;
        }

        fileHandleController.getNewBaseNames().getItems().setAll(fileHandleController.getBaseNames().getItems());

        List<String> renamedFiles = findCurrentAddNameLocationAndProcessFiles();

        if(renamedFiles.size() > 0)
            fileHandleController.getNewBaseNames().getItems().setAll(renamedFiles);

    }

    private List<String> findCurrentAddNameLocationAndProcessFiles() {
        return fileHandleController.getNewBaseNames().getItems().stream()
                .map(this::addTextToNewBaseName)
                .collect(Collectors.toList());
    }

    private String addTextToNewBaseName(String baseName) {
        String[] parts = baseName.split(LAST_PERIOD_LOOKAHEAD_REGEX);
        String fileName = parts[0];
        if(fileHandleController.getCurrentAddTextNameLocationOption().compareTo(NameLocation.AFTER) == 0)
            parts[0] = StringUtils.prependIfMissing(fileHandleController.getAddText().getText(), fileName);
        else
            parts[0] = StringUtils.appendIfMissing(fileHandleController.getAddText().getText(), fileName);

        return StringUtils.join(parts, ".");
    }

    public void addFiles(ActionEvent actionEvent) {
        fileHandleController.setFiles(fileHandleController.getFileChooser().showOpenMultipleDialog(null));
        if(fileHandleController.getFiles() != null) {
            List<String> rawBaseNames = fileHandleController.getFiles().stream()
                    .map(File::getName)
                    .collect(Collectors.toList());

            fileHandleController.getBaseNames().getItems().setAll(rawBaseNames);
            fileHandleController.getNewBaseNames().getItems().setAll(rawBaseNames);
        } else {
            fileHandleController.showAlertWithMessage("There was an issue selecting your files. Please try again.");
        }
    }
}

