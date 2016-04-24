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

// TODO: Refactor to make code DRY
// TODO: Extract strings into constants
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
    private ListView<String> baseNames = new ListView<>();

    @FXML
    private ListView<String> newBaseNames = new ListView<>();

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

    private final FileChooser fileChooser = new FileChooser();
    private final static String USER_DIRECTORY = System.getProperty("user.home");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.setInitialDirectory(new File(USER_DIRECTORY));
        currentReplaceOption = RenameOption.REPLACE;

        initAlert();
        initMenuBar();
        initRenameOptions();
        initRenameButton();
        initNameLocations();
        initFormatOptions();
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
        renameOptions.setTooltip(new Tooltip("Select an option to rename your files"));
        renameOptions.setOnAction(this::renameOptionsAction);
    }

    private void renameOptionsAction(ActionEvent actionEvent) {
        RenameOption option = RenameOption.values()[renameOptions.getSelectionModel().getSelectedIndex()];

        switch (option) {
            case REPLACE:
                showGroup(replaceGroup);
                updateRenameAction(this::replaceBaseNames);
                break;
            case ADD:
                showGroup(addGroup);
                updateRenameAction(this::addToBaseNames);
                break;
            case FORMAT:
                showGroup(formatGroup);
                updateRenameAction(this::formatBaseNames);
                break;
            default:
                break;
        }

        currentReplaceOption = option;
        getStage().sizeToScene();
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

    private void replaceBaseNames(ActionEvent event) {
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
                Path movedFile = Files.move(oldFilePath, oldFilePath.resolveSibling(newBaseNames.getItems().get(i)));
                movedFiles.add(movedFile.toFile());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        baseNames.getItems().setAll(newBaseNames.getItems());
        files = movedFiles;
    }

    private void addToBaseNames(ActionEvent event) {
        performFileRename();
    }

    private void formatBaseNames(ActionEvent event) {
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
        if(files == null || files.size() == 0 || baseNames == null || baseNames.getItems().size() == 0) {
            showAlertWithMessage("You haven't added any files to rename yet.");
            return;
        }

        newBaseNames.getItems().setAll(baseNames.getItems());

        List<String> renamedFiles = newBaseNames.getItems().stream()
                .filter(fileName -> fileName.contains(findText.getText()))
                .map(fileName -> fileName.replaceAll(findText.getText(), replaceText.getText()))
                .collect(Collectors.toList());

        if(renamedFiles.size() > 0)
            newBaseNames.getItems().setAll(renamedFiles);
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
        if(files == null || files.size() == 0 || baseNames == null || baseNames.getItems().size() == 0) {
            showAlertWithMessage("You haven't added any files to rename yet.");
            return;
        } else if(currentAddNameLocationOption == null) {
            showAlertWithMessage("You haven't chosen the location to add your new text yet.");
            return;
        }

        newBaseNames.getItems().setAll(baseNames.getItems());

        List<String> renamedFiles = findCurrentAddNameLocationAndProcessFiles();

        if(renamedFiles.size() > 0)
            newBaseNames.getItems().setAll(renamedFiles);

    }

    // TODO: Remove the duplication in these three methods
    private List<String> findCurrentAddNameLocationAndProcessFiles() {
        List<String> renamedFiles;
        if(currentAddNameLocationOption.compareTo(NameLocation.AFTER) == 0) {
            renamedFiles = newBaseNames.getItems().stream()
                    .map(this::prependAdditionalText)
                    .collect(Collectors.toList());
        } else {
            renamedFiles = newBaseNames.getItems().stream()
                    .map(this::appendAdditionalText)
                    .collect(Collectors.toList());
        }
        return renamedFiles;
    }

    private String appendAdditionalText(String baseName) {
        String[] parts = baseName.split("\\.(?=[^\\.]+$)");
        String fileName = parts[0];
        parts[0] = StringUtils.appendIfMissing(addText.getText(), fileName);
        return StringUtils.join(parts, ".");
    }

    private  String prependAdditionalText(String baseName) {
        String[] parts = baseName.split("\\.(?=[^\\.]+$)");
        String fileName = parts[0];
        parts[0] = StringUtils.prependIfMissing(addText.getText(), fileName);
        return StringUtils.join(parts, ".");
    }

    public void addFiles(ActionEvent actionEvent) {
        files = fileChooser.showOpenMultipleDialog(null);
        if(files != null) {
            List<String> rawBaseNames = files.stream()
                    .map(File::getName)
                    .collect(Collectors.toList());

            baseNames.getItems().setAll(rawBaseNames);
            newBaseNames.getItems().setAll(rawBaseNames);
        } else {
            showAlertWithMessage("There was an issue selecting your files. Please try again.");
        }
    }

    public void closeApp(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

}
