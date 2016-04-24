package com.wembleyleach.filehandle;

import com.wembleyleach.filehandle.enums.*;
import javafx.application.Platform;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileHandleController implements Initializable {

    @FXML
    private MenuBar menuBar;

    @FXML
    private ComboBox<RenameOption> renameOptions;

    @FXML
    private ComboBox<NameLocation> nameLocationOptions;

    @FXML
    private ComboBox<NameLocation> formatNameLocationOptions;

    @FXML
    private ComboBox<NameFormat> nameFormatOptions;

    @FXML
    private Button cancel;

    @FXML
    private Button rename;

    @FXML
    private TextField findText;

    @FXML
    private TextField replaceText;

    @FXML
    private TextField addText;

    @FXML
    private TextField customFormat;

    @FXML
    private TextField startAt;

    @FXML
    private ListView<String> fileNames = new ListView<>();

    @FXML
    private ListView<String> newFileNames = new ListView<>();

    private List<File> files;

    @FXML
    private Group addGroup;

    @FXML
    private Group replaceGroup;

    @FXML
    private Group formatGroup;

    private Stage primaryStage;
    private Alert alert;
    private RenameOption currentReplaceOption;
    private NameLocation currentAddNameLocationOption;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initAlert();
        initMenuBar();
        initRenameOptions();
        initRenameButton();
        initNameLocations();
        initFormatOptions();
        currentReplaceOption = RenameOption.REPLACE;
        showGroup(replaceGroup);
    }

    private void initAlert() {
        alert = new Alert(Alert.AlertType.WARNING);
    }

    private void initMenuBar() {
        menuBar.setUseSystemMenuBar(true);
    }

    private void initRenameOptions() {
        renameOptions.getItems().addAll(RenameOption.values());
        renameOptions.setTooltip(new Tooltip("Select an option to rename your originalFiles"));
        renameOptions.setOnAction(event -> {
            RenameOption option = RenameOption.values()[renameOptions.getSelectionModel().getSelectedIndex()];

            switch (option) {
                case REPLACE:
                    showGroup(replaceGroup);
                    updateRenameAction(this::replaceFileNames);
                    break;
                case ADD:
                    showGroup(addGroup);
                    updateRenameAction(this::addToFileNames);
                    break;
                case FORMAT:
                    showGroup(formatGroup);
                    updateRenameAction(this::formatFileNames);
                    break;
                default:
                    break;
            }

            currentReplaceOption = option;
            getStage().sizeToScene();
        });
    }

    private void showGroup(Group group) {
        hideGroups();
        group.setVisible(true);
        group.setManaged(true);
    }

    private void hideGroups() {
        Arrays.asList(replaceGroup, addGroup, formatGroup)
                .forEach(FileHandleController::hideGroup);
    }

    private static void hideGroup(Group group) {
        group.setVisible(false);
        group.setManaged(false);
    }

    private void updateRenameAction(EventHandler<ActionEvent> event) {
        rename.setOnAction(event);
    }

    private void replaceFileNames(ActionEvent event) {
        if(files == null || files.size() == 0 || fileNames == null || fileNames.getItems().size() == 0) {
            showAlertWithMessage("You haven't added any files to rename yet.");
            return;
        }

        performFileRename();
    }

    private void showAlertWithMessage(String message) {
        alert.setContentText(message);
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> alert.close());
    }

    private void performFileRename() {
        List<File> movedFiles = new ArrayList<>(files.size());
        for(int i = 0; i < files.size(); i++) {
            try {
                Path oldFilePath = files.get(i).toPath();
                Path movedFile = Files.move(oldFilePath, oldFilePath.resolveSibling(newFileNames.getItems().get(i)));
                movedFiles.add(movedFile.toFile());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileNames.getItems().setAll(newFileNames.getItems());
        files = movedFiles;
    }

    private void addToFileNames(ActionEvent event) {
        System.out.println("Current add option: " + currentAddNameLocationOption);
        if(files == null || files.size() == 0 || fileNames == null || fileNames.getItems().size() == 0) {
            showAlertWithMessage("You haven't added any files to rename yet.");
            return;
        } else if(currentAddNameLocationOption == null) {
            showAlertWithMessage("You haven't chosen the location to add your new text yet.");
            return;
        }
    }

    private void formatFileNames(ActionEvent event) {
        System.out.println("Format file names called");
    }

    private Stage getStage() {
        if(primaryStage == null)
            primaryStage = (Stage) renameOptions.getScene().getWindow();

        return primaryStage;
    }

    private void initRenameButton() {
        rename.setOnAction(event -> {
            showAlertWithMessage("You haven't selected an option to use to rename your files yet.");
        });
    }

    private void initNameLocations() {
        nameLocationOptions.getItems().addAll(NameLocation.values());
        nameLocationOptions.setTooltip(new Tooltip("Select where your text should be applied."));
        nameLocationOptions.setOnAction(event -> {
            currentAddNameLocationOption = NameLocation.values()[nameLocationOptions.getSelectionModel().getSelectedIndex()];
        });
    }

    private void initFormatOptions() {
        nameFormatOptions.getItems().addAll(NameFormat.values());
        nameFormatOptions.setTooltip(new Tooltip("Select a format to use when renaming your files."));
        formatNameLocationOptions.getItems().addAll(NameLocation.values());
        formatNameLocationOptions.setTooltip(new Tooltip("Select where your text should be applied."));
    }

    private void performRenameLivePreview() {
        newFileNames.getItems().setAll(fileNames.getItems());

        List<String> renamedFiles = newFileNames.getItems().stream()
                .filter(fileName -> fileName.contains(findText.getText()))
                .map(fileName -> fileName.replaceAll(findText.getText(), replaceText.getText()))
                .collect(Collectors.toList());

        if(renamedFiles.size() > 0)
            newFileNames.getItems().setAll(renamedFiles);
    }

    public void livePreviewFileRename(KeyEvent event) {
        boolean shouldDisableRename = ((TextField) event.getSource()).getText().trim().isEmpty();
        rename.setDisable(shouldDisableRename);
        if(!shouldDisableRename)  {
            switch (currentReplaceOption) {
                case REPLACE:
                    performRenameLivePreview();
                    break;
                case ADD:
                    performAddTextLivePreview();
                    break;
                case FORMAT:
                    performFormatLivePreview();
                    break;
                default:
                    break;
            }
        }
    }

    private void performFormatLivePreview() {
        System.out.println("Performing format live preview.");
    }

    private void performAddTextLivePreview() {
        if(currentAddNameLocationOption == null) {
            showAlertWithMessage("You haven't chosen the location to add your new text yet.");
            return;
        }

        newFileNames.getItems().setAll(fileNames.getItems());

        List<String> renamedFiles;
        if(currentAddNameLocationOption.compareTo(NameLocation.AFTER) == 0) {
            renamedFiles = newFileNames.getItems().stream()
                    .map(fileName -> StringUtils.appendIfMissing(addText.getText(), fileName))
                    .collect(Collectors.toList());
        } else {
            renamedFiles = newFileNames.getItems().stream()
                    .map(fileName -> StringUtils.prependIfMissing(addText.getText(), fileName))
                    .collect(Collectors.toList());
        }

        if(renamedFiles.size() > 0)
            newFileNames.getItems().setAll(renamedFiles);

    }

    public void addFiles(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        String userDirectory = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userDirectory));
        files = fileChooser.showOpenMultipleDialog(null);
        if(files != null) {
            List<String> rawFileNames = files.stream()
                    .map(File::getName)
                    .collect(Collectors.toList());

            fileNames.getItems().setAll(rawFileNames);
            newFileNames.getItems().setAll(rawFileNames);
        } else {
            showAlertWithMessage("There was an issue selecting your files. Please try again.");
        }
    }

    public void closeApp(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }



}
