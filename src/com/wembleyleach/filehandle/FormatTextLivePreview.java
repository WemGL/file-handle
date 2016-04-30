package com.wembleyleach.filehandle;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.wembleyleach.filehandle.enums.NameFormat.*;
import static com.wembleyleach.filehandle.enums.NameLocation.*;

public class FormatTextLivePreview implements LivePreviewBehavior {

    private final FileHandleController fileHandleController;
    private static final String BASE_NAME_LOOKAHEAD_REGEX = "^.*(?=\\.)";
    private static final String LAST_PERIOD_LOOKAHEAD_REGEX = "\\.(?=[^\\.]+$)";
    private int startValue = 0;
    private String dateTime = "";


    public FormatTextLivePreview(FileHandleController fileHandleController) {
        this.fileHandleController = fileHandleController;
    }

    @Override
    public void livePreview() {
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

        fileHandleController.getNewBaseNames().getItems().setAll(fileHandleController.getBaseNames().getItems());

        if(fileHandleController.getCurrentNameFormatOption() == INDEX ||
                fileHandleController.getCurrentNameFormatOption() == COUNTER) {
            startValue = fileHandleController.getStartAt().getText().trim().isEmpty()
                    ? 0
                    : Integer.parseInt(fileHandleController.getStartAt().getText());
        } else {
            startValue = 1;
            LocalDateTime localDateTime = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH.mm.ss a");
            dateTime = String.format("%s at %s", dateFormatter.format(localDateTime), timeFormatter.format(localDateTime));
        }

        List<String> renamedFiles = fileHandleController.getNewBaseNames().getItems().stream()
                .map(this::performFormatUsingCurrentFormatOption)
                .collect(Collectors.toList());

        if(renamedFiles.size() > 0)
            fileHandleController.getNewBaseNames().getItems().setAll(renamedFiles);

    }

    private String performFormatUsingCurrentFormatOption(String baseName) {
        String newBaseName = createNewBaseNameForCustomFormat(baseName);
        switch (fileHandleController.getCurrentNameFormatOption()) {
            case INDEX:
            case COUNTER:
                return performAddIndexOrCounter(newBaseName);
            case DATE:
                return performAddDate(newBaseName);
            default:
                return "";
        }
    }

    private String createNewBaseNameForCustomFormat(String baseName) {
        return StringUtils.replacePattern(baseName, BASE_NAME_LOOKAHEAD_REGEX, fileHandleController.getCustomFormat().getText());
    }

    private String performAddIndexOrCounter(String baseName) {
        String formattedBaseName = "";
        if(fileHandleController.getCurrentNameFormatOption() == INDEX) {
            formattedBaseName = Integer.toString(startValue++);
        } else if(fileHandleController.getCurrentNameFormatOption() == COUNTER) {
            formattedBaseName = String.format("%05d", startValue++);
        }

        String[] baseNameParts = baseName.split(LAST_PERIOD_LOOKAHEAD_REGEX);
        if(fileHandleController.getCurrentNameFormatLocationOption() == AFTER) {
            baseNameParts[0] = StringUtils.appendIfMissing(baseNameParts[0], formattedBaseName);
        } else if(fileHandleController.getCurrentNameFormatLocationOption() == BEFORE) {
            baseNameParts[0] = StringUtils.prependIfMissing(baseNameParts[0], formattedBaseName);
        }

        return StringUtils.join(baseNameParts, ".");
    }

    private String performAddDate(String newBaseName) {
        String[] baseNameParts = newBaseName.split(LAST_PERIOD_LOOKAHEAD_REGEX);
        if(fileHandleController.getCurrentNameFormatLocationOption() == AFTER) {
            baseNameParts[0] = StringUtils.appendIfMissing(baseNameParts[0], String.format("%s %d", dateTime, startValue++));
        } else {
            baseNameParts[0] = StringUtils.prependIfMissing(baseNameParts[0], String.format("%s %d", dateTime, startValue++));
        }

        return StringUtils.join(baseNameParts, ".");
    }

}

