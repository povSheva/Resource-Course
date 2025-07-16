package com.example.demo.ui;

import com.example.demo.controllers.FilterController;
import com.example.demo.controllers.OpenFileController;
import com.example.demo.controllers.ResourceController;
import com.example.demo.controllers.SearchController;
import com.example.demo.dao.FileAdditionalDao;
import com.example.demo.dao.FileEntityDao;
import com.example.demo.entity.FileAdditionalEntity;
import com.example.demo.entity.FileEntity;
import com.example.demo.service.FileAdditionalEntityService;
import com.example.demo.service.FileEntityService;
import java.util.HashMap;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.nio.file.Paths;
import java.nio.file.Path;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;



/**
 * JavaFX‑приложение для управления файлами.
 * <p>
 * Вся работа с базой и файловой системой делегирована {@link FileEntityService},
 * чтобы UI не знал ничего о {@link java.sql.Connection} и {@link java.nio.file.Path}.
 */
public class ResourceManagerApp extends Application {

    private final FileEntityService fileService =
            new FileEntityService(
                    new FileEntityDao(),
                    System.getProperty("repo.root", "exports")
            );
    private final FileAdditionalEntityService metaService =
            new FileAdditionalEntityService(new FileAdditionalDao());
    private final ObservableList<FileEntity> fileItems = FXCollections.observableArrayList();

    private final ObservableList<FileEntity> pinnedItems = FXCollections.observableArrayList();

    private SearchController searchController;

    private ListView<FileEntity> fileListView;

    private FilterController filterCtrl;

    private TextField searchField;

    private OpenFileController opener;

    // Preview-area controls (правый блок)
    private Label      previewTitle;
    private TextFlow   previewDesc;
    private Label      previewTag;
    private Label      previewDate;
    private Button     forwardBtn;
    private ToggleButton pinBtn;
    private Button     deleteBtn;

    private double xOffset; // для перетаскивания окна
    private double yOffset;

    /* ===================================================================== */
    /* === Application entry‑point ======================================== */

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        // Открывалка файлов
        Path repoRoot = Paths.get(System.getProperty("repo.root", "exports"));
        opener = new OpenFileController(repoRoot, getHostServices());

        // Загрузка всех файлов из сервиса
        List<FileEntity> all = fileService.findAll();
        fileItems.setAll(all);

        // *** Инициализация списка pinnedItems из метаданных ***
        all.stream()
                .filter(f -> metaService.getOrCreateMetadata(f.getUuid()).isPinned())
                .forEach(pinnedItems::add);

        // Контроллеры для фильтрации и поиска
        filterCtrl       = new FilterController(fileItems);
        searchController = new SearchController(filterCtrl);

        HBox titleBar = buildTitleBar(primaryStage);

        // Собираем основной контент
        BorderPane content = new BorderPane();
        content.setLeft(buildLeftBar(filterCtrl));
        content.setCenter(buildCenter());
        content.setRight(buildPreviewBox());

        // Округлённое окно
        VBox root = wrapWithRoundedWindow(titleBar, content);

        Scene scene = new Scene(root, 900, 600);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /* ===================================================================== */
    /* === Title bar (traffic lights, search, export) ====================== */

    private HBox buildTitleBar(Stage stage) {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(12, 20, 12, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");

        // Traffic‑lights buttons ------------------------------------------
        StackPane btnClose = makeTrafficButton(Color.web("#ff5f57"), "×", e -> stage.close());
        StackPane btnMin   = makeTrafficButton(Color.web("#ffbd2e"), "–", e -> animateMinimize(stage));
        StackPane btnMax   = makeTrafficButton(Color.web("#28c840"), "+", e -> stage.setMaximized(!stage.isMaximized()));
        var lights = Stream.of(btnClose, btnMin, btnMax).toList();
        lights.forEach(btn -> {
            btn.setOnMouseEntered(ev -> lights.forEach(ResourceManagerApp::showGlyph));
            btn.setOnMouseExited (ev -> lights.forEach(ResourceManagerApp::hideGlyph));
        });

        // Search box -------------------------------------------------------
        searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setPrefWidth(420);
        searchField.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; " +
                "-fx-border-color: #d0d4d9; -fx-background-color: #f9fafb; " +
                "-fx-padding: 6 12 6 36; " +
                "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        searchField.textProperty().addListener((obs, old, nw) ->
                searchController.onSearch(nw)
        );
        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);
        // Иконку поиска в нормальное место подвинул
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, -385));
        StackPane searchBox = new StackPane(searchField, searchIcon);

        // Export button ----------------------------------------------------
        Button exportBtn = new Button("Export");
        exportBtn.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8; " +
                "-fx-font-size: 15px; -fx-font-family: 'Inter Semibold'; -fx-font-weight: 600; " +
                "-fx-text-fill: #0f1113; -fx-padding: 6 32; -fx-border-color: transparent;");

        ResourceController resCtrl = new ResourceController(fileService, fileItems, filterCtrl);

        exportBtn.setOnAction(resCtrl::onExportClick);

        Region leftSpacer  = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        bar.getChildren().setAll(btnClose, btnMin, btnMax, leftSpacer, searchBox, rightSpacer, exportBtn);

        // Dragging the window ---------------------------------------------
        bar.setOnMousePressed(e -> { xOffset = e.getSceneX(); yOffset = e.getSceneY(); });
        bar.setOnMouseDragged(e -> {
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            }
        });

        stage.iconifiedProperty().addListener((obs, wasMin, isMin) -> {
            if (!isMin) restoreAnimation((Region) stage.getScene().getRoot());
        });

        return bar;
    }

    /* ===================================================================== */
    /* === Left bar (pinned + filter) ===================================== */

    private VBox buildLeftBar(FilterController filterCtrl) {

        Label pinnedLabel = new Label("  Pinned");
        pinnedLabel.setStyle("-fx-font-weight: bold;");
        pinnedLabel.setTranslateY(6);

        Separator sepPinned = new Separator();
        sepPinned.setPrefWidth(220);
        HBox sepPinnedWrapper = new HBox(sepPinned);
        sepPinnedWrapper.setAlignment(Pos.CENTER);

        ListView<FileEntity> pinnedList = new ListView<>(pinnedItems);
        pinnedList.setMaxHeight(100);
        pinnedList.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-radius: 8;"
        );
        pinnedList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FileEntity item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getOrigName());
            }
        });

        pinnedList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                FileEntity sel = pinnedList.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    try { opener.open(sel); }
                    catch (Exception ex) { showError("Не удалось открыть файл", ex); }
                }
            }
        });

        Button addBtn = new Button("+ Добавить ресурс");
        addBtn.setMaxWidth(Region.USE_COMPUTED_SIZE);
        addBtn.setOnAction(e -> onAddResource());
        HBox btnWrapper = new HBox(addBtn);
        btnWrapper.setAlignment(Pos.CENTER);
        btnWrapper.setPadding(new Insets(4, 0, 4, 0));

        Separator sepAfterBtn = new Separator();
        sepAfterBtn.setPrefWidth(220);
        HBox sepAfterBtnWrapper = new HBox(sepAfterBtn);
        sepAfterBtnWrapper.setAlignment(Pos.CENTER);

        Label filterLabel = new Label("  Filters");
        filterLabel.setStyle("-fx-font-weight: bold;");
        filterLabel.setTranslateY(-2);

        Separator sepAfterFilter = new Separator();
        sepAfterFilter.setPrefWidth(220);
        HBox sepAfterFilterWrapper = new HBox(sepAfterFilter);
        sepAfterFilterWrapper.setAlignment(Pos.CENTER);

        VBox filterBox = new VBox(8);
        filterBox.setPadding(new Insets(8));
        filterBox.setMaxHeight(160);

        // Для удаления строк по типу
        Map<String, Node> rowMap = new HashMap<>();

        // Изначальные фильтры
        filterCtrl.getTypeChecks().forEach((type, prop) -> {
            GridPane row = makeRow(type, prop);
            rowMap.put(type, row);
            filterBox.getChildren().add(row);
        });

        // Когда добавляется новый тип
        filterCtrl.addTypeAddedListener(entry -> Platform.runLater(() -> {
            String type = entry.getKey();
            BooleanProperty prop = entry.getValue();
            GridPane row = makeRow(type, prop);
            rowMap.put(type, row);
            filterBox.getChildren().add(row);
        }));

        // Когда тип «умирает»
        filterCtrl.addTypeRemovedListener(type -> Platform.runLater(() -> {
            Node row = rowMap.remove(type);
            if (row != null) filterBox.getChildren().remove(row);
        }));

        return new VBox(10,
                pinnedLabel,
                sepPinnedWrapper,
                pinnedList,
                btnWrapper,
                sepAfterBtnWrapper,
                filterLabel,
                sepAfterFilterWrapper,
                filterBox
        );
    }


    /** создаёт строку «текст  ––  чекбокс» и биндит property */
    private GridPane makeRow(String type, BooleanProperty prop) {
        CheckBox cb = new CheckBox();
        cb.selectedProperty().bindBidirectional(prop);
        return createFilterRow(type, cb);
    }

    // вспомогательный метод для создания строк фильтров
    private GridPane createFilterRow(String labelText, CheckBox cb) {
        Label label = new Label(labelText);
        cb.setText("");

        GridPane row = new GridPane();
        row.setHgap(45);
        row.setAlignment(Pos.CENTER_LEFT);

        ColumnConstraints col1 = new ColumnConstraints(70);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHalignment(HPos.RIGHT);

        row.getColumnConstraints().addAll(col1, col2);
        row.add(label, 0, 0);
        row.add(cb,    1, 0);
        return row;
    }

    /* ===================================================================== */
    /* === Center: ListView with files ===================================== */

    private VBox buildCenter() {
        ListView<FileEntity> listView = createFileList();
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) clearPreview();
            else showPreview(newVal);
        });

        Label status = new Label();
        status.textProperty().bind(Bindings.size(fileItems).asString("%d записей"));

        VBox box = new VBox(5, listView, status);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private ListView<FileEntity> createFileList() {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        ListView<FileEntity> lv = new ListView<>(fileItems);

        lv.getStylesheets().add(
                getClass().getResource("/css/listview-custom.css")
                        .toExternalForm()
        );

        lv.setCellFactory(view -> new ListCell<>() {

            private final HBox row = new HBox(10);
            private final ImageView icon = new ImageView();
            private final TextFlow nameFlow = new TextFlow();
            private final Label type = new Label();
            private final Label date = new Label();
            private final Button moreBtn = new Button("•••");

            {

                row.getStyleClass().add("file-row");

                icon.setFitWidth(24);
                icon.setFitHeight(24);

                type.setTextFill(Color.web("#374151"));
                date.setTextFill(Color.web("#374151"));

                moreBtn.setFocusTraversable(false);
                moreBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16;");

                HBox.setHgrow(nameFlow, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8));

                row.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-background-radius: 8;"
                );

                row.getChildren().addAll(icon, nameFlow, type, date, moreBtn);
            }


            @Override
            protected void updateItem(FileEntity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                // Иконка по типу
                URL resUrl = getClass().getResource(switch (item.getType().toUpperCase()) {
                    case "PDF"   -> "/images/pdf.png";
                    case "IMAGE" -> "/images/image.png";
                    default      -> "/images/file.png";
                });
                icon.setImage(resUrl != null ? new Image(resUrl.toExternalForm()) : null);

                // Текст с обрезкой и подсветкой
                nameFlow.getChildren().clear();
                String fullName = item.getOrigName();
                String search   = searchField.getText();
                double listW    = getListView().getWidth();
                double fixedW   = 24 + 10 + type.getWidth() + 10 + date.getWidth() + 40;
                int maxChars = (int) (Math.max(listW - fixedW, 60) / 7) - 8;
                String shown = fullName.length() > maxChars
                        ? fullName.substring(0, Math.max(maxChars - 1, 1)) + "…"
                        : fullName;

                if (search != null && !search.isBlank()) {
                    String q  = search.toLowerCase();
                    String lo = shown.toLowerCase();
                    int idx   = lo.indexOf(q);
                    if (idx >= 0) {
                        Text before = new Text(shown.substring(0, idx));
                        Label match = new Label(shown.substring(idx, idx + q.length()));
                        match.setStyle("-fx-background-color: yellow; -fx-background-radius: 4;");
                        Text after  = new Text(shown.substring(idx + q.length()));
                        nameFlow.getChildren().addAll(before, match, after);
                    } else {
                        nameFlow.getChildren().add(new Text(shown));
                    }
                } else {
                    nameFlow.getChildren().add(new Text(shown));
                }

                type.setText(item.getType());
                date.setText(item.getAddedAt().format(fmt));

                // Обработчик кнопки «•••» — редактирование тега и доп. информации
                moreBtn.setOnAction(evt -> {
                    Dialog<FileAdditionalEntity> dlg = new Dialog<>();
                    dlg.setTitle("Редактировать метаданные");
                    ButtonType saveBtn = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
                    dlg.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

                    TextField tagInput = new TextField();
                    tagInput.setPromptText("Tag");
                    TextArea infoInput = new TextArea();
                    infoInput.setPromptText("Additional info");
                    infoInput.setPrefRowCount(4);

                    // Загружаем или создаём метаданные
                    FileAdditionalEntity existing = metaService.getOrCreateMetadata(item.getUuid());
                    tagInput.setText(existing.getTag());
                    infoInput.setText(existing.getAdditionalInfo());

                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.add(new Label("Tag:"), 0, 0);
                    grid.add(tagInput,        1, 0);
                    grid.add(new Label("Info:"),0, 1);
                    grid.add(infoInput,       1, 1);
                    dlg.getDialogPane().setContent(grid);

                    dlg.setResultConverter(button -> {
                        if (button == saveBtn) {
                            existing.setTag(tagInput.getText());
                            existing.setAdditionalInfo(infoInput.getText());
                            existing.setUpdatedAt(LocalDateTime.now());
                            return existing;
                        }
                        return null;
                    });

                    dlg.showAndWait()
                            .filter(meta -> meta != null)
                            .ifPresent(meta -> {
                                metaService.saveMetadata(meta);
                                // если этот файл сейчас выбран — обновляем превью
                                FileEntity sel = getSelectedFile();
                                if (sel != null && sel.getUuid().equals(item.getUuid())) {
                                    showPreview(item);
                                }
                            });
                });
                setGraphic(row);
            }
        });

        // это скругнение главного списка файлов
        lv.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-radius: 8;"
        );

        lv.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                FileEntity sel = lv.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                try {
                    opener.open(sel);
                } catch (Exception ex) {
                    showError("Не удалось открыть файл", ex);
                }
            }
        });

        lv.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            Platform.runLater(() -> {
                for (Node node : lv.lookupAll(".scroll-bar")) {
                    if (node instanceof ScrollBar sb &&
                            sb.getOrientation() == Orientation.HORIZONTAL) {
                        sb.setVisible(false);
                        sb.setManaged(false);
                    }
                }
                lv.refresh();
            });
        });


        // тут крч скрываем нижний скролл у таблицы
        lv.getStylesheets().add(
                getClass().getResource("/css/no-h-scroll.css")
                        .toExternalForm()
        );

        //lv.setPrefHeight(400);

        VBox.setVgrow(lv, Priority.ALWAYS);
        fileListView = lv;
        return lv;
    }

    private VBox buildPreviewBox() {
        // 1) Заголовок
        previewTitle = new Label();
        previewTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        // включаем перенос и фиксируем ширину

        // 2) Описание (additionalInfo)
        previewDesc = new TextFlow();
        previewDesc.setPrefWidth(260);
        previewDesc.setLineSpacing(4);
        // TextFlow сам будет переносить текст по ширине, заданной через prefWidth

        // 3) Метаданные: Тег + Дата
        previewTag  = new Label();
        previewDate = new Label();
        previewTag.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        previewDate.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        // про запас тоже включим перенос, если тег слишком длинный
        previewTag.setWrapText(true);
        previewTag.setMaxWidth(200);
        HBox metaBox = new HBox(20, previewTag, previewDate);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        // 4) Спэйсер, чтобы "опустить" мета-блок вниз
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // 5) Разделительная линия
        Separator sep = new Separator();

        // 6) Кнопки действий
        forwardBtn = new Button("Переслать");
        forwardBtn.setDisable(true);
        // ... (ваша логика)

        pinBtn = new ToggleButton("🔖");
        pinBtn.setDisable(true);
        pinBtn.setOnAction(e -> {
            FileEntity sel = getSelectedFile();
            if (sel != null) {
                var meta = metaService.getOrCreateMetadata(sel.getUuid());
                meta.setPinned(pinBtn.isSelected());
                meta.setUpdatedAt(LocalDateTime.now());
                metaService.saveMetadata(meta);
                // обновление pinned‑списка и т.п.
            }
        });


        deleteBtn = new Button("✖");
        deleteBtn.setDisable(true);
        deleteBtn.setOnAction(e -> {
            FileEntity sel = getSelectedFile();
            if (sel != null) {
                fileService.deleteFile(sel.getUuid());
                filterCtrl.onRemove(sel);
                fileItems.remove(sel);
                clearPreview();
            }
        });


        HBox actions = new HBox(10, forwardBtn, pinBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER);

        // 7) Собираем всё в порядке: заголовок, описание, spacer, мета, разделитель, кнопки
        VBox box = new VBox(10,
                previewTitle,
                previewDesc,
                spacer,
                metaBox,
                sep,
                actions
        );
        box.setPadding(new Insets(16));
        box.setPrefWidth(300);
        box.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: #E0E0E0;"
        );

        return box;
    }



    private void showPreview(FileEntity file) {
        // 1) Заголовок
        previewTitle.setText(file.getOrigName());

        // 2) Описание
        FileAdditionalEntity meta = metaService.getOrCreateMetadata(file.getUuid());
        String info = meta.getAdditionalInfo();
        previewDesc.getChildren().setAll(new Text(info != null && !info.isBlank() ? info : "нет описания..."));

        // 3) Тег и дата над линией
        String tag = meta.getTag();
        previewTag.setText("Тег: " + (tag != null && !tag.isBlank() ? tag : "без тега"));

        String formattedDate = meta.getUpdatedAt()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        previewDate.setText("Обновлён: " + formattedDate);

        pinBtn.setDisable(false);
        pinBtn.setSelected(meta.isPinned());
        // *** 4) После сохранения метаданных обновляем список
        pinBtn.setOnAction(e -> {
            FileEntity sel = getSelectedFile();
            if (sel != null) {
                var m = metaService.getOrCreateMetadata(sel.getUuid());
                boolean nowPinned = pinBtn.isSelected();
                m.setPinned(nowPinned);
                m.setUpdatedAt(LocalDateTime.now());
                metaService.saveMetadata(m);

                // добавляем или удаляем из pinnedItems
                if (nowPinned) {
                    if (!pinnedItems.contains(sel)) pinnedItems.add(sel);
                } else {
                    pinnedItems.remove(sel);
                }
            }
        });

        // 4) Включаем кнопки
        forwardBtn.setDisable(false);
        pinBtn.setDisable(false);
        pinBtn.setSelected(meta.isPinned());
        deleteBtn.setDisable(false);
    }

    private void clearPreview() {
        previewTitle.setText("");
        previewDesc.getChildren().clear();
        previewTag.setText("");
        previewDate.setText("");
        forwardBtn.setDisable(true);
        pinBtn.setDisable(true);
        pinBtn.setSelected(false);
        deleteBtn.setDisable(true);
    }

    private FileEntity getSelectedFile() {
        if (fileListView != null) {
            return fileListView.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    private Parent getSceneRoot() {
        return forwardBtn.getScene().getRoot();
    }


    private void onAddResource() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        try {
            // вместо service.uploadFile → fileService.uploadFile
            FileEntity saved = fileService.uploadFile(file);
            searchController.onAddNew(saved);
        } catch (Exception ex) {
            showError("Не удалось добавить файл", ex);
        }
    }

    /* ===================================================================== */
    /* === Helpers ========================================================= */

    private static StackPane makeTrafficButton(Color color, String glyph, EventHandler<MouseEvent> action) {
        Circle bg = new Circle(9, color);
        Label lbl = new Label(glyph);
        lbl.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        lbl.setMouseTransparent(true); lbl.setVisible(false);

        StackPane sp = new StackPane(bg, lbl);
        sp.setPrefSize(20, 20);
        sp.setCursor(Cursor.HAND);
        sp.setOnMouseClicked(action);
        sp.setUserData(lbl);
        return sp;
    }

    private static void showGlyph(StackPane b) { ((Label) b.getUserData()).setVisible(true); }
    private static void hideGlyph(StackPane b) { ((Label) b.getUserData()).setVisible(false); }

    private void animateMinimize(Stage st) {
        Region root = (Region) st.getScene().getRoot();
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.scaleXProperty(), 1),
                        new KeyValue(root.scaleYProperty(), 1),
                        new KeyValue(root.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(root.scaleXProperty(), .4, Interpolator.EASE_BOTH),
                        new KeyValue(root.scaleYProperty(), .4, Interpolator.EASE_BOTH),
                        new KeyValue(root.opacityProperty(), 0, Interpolator.EASE_BOTH))
        );
        tl.setOnFinished(e -> st.setIconified(true));
        tl.play();
    }

    private void restoreAnimation(Region root) {
        root.setScaleX(.4); root.setScaleY(.4); root.setOpacity(0);
        Timeline back = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.scaleXProperty(), .4),
                        new KeyValue(root.scaleYProperty(), .4),
                        new KeyValue(root.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(root.scaleXProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(root.scaleYProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(root.opacityProperty(), 1, Interpolator.EASE_BOTH))
        );
        back.play();
    }

    private VBox wrapWithRoundedWindow(HBox titleBar, BorderPane content) {
        VBox rootPane = new VBox(titleBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);

        CornerRadii radii = new CornerRadii(15);
        rootPane.setBackground(new Background(new BackgroundFill(Color.WHITE, radii, Insets.EMPTY)));
        rootPane.setBorder(new Border(new BorderStroke(Color.web("#cccccc"), BorderStrokeStyle.SOLID, radii, new BorderWidths(1))));

        Rectangle clip = new Rectangle();
        clip.setArcWidth(30); clip.setArcHeight(30);
        rootPane.setClip(clip);
        rootPane.layoutBoundsProperty().addListener((obs, o, n) -> {
            clip.setWidth(n.getWidth()); clip.setHeight(n.getHeight());
        });
        return rootPane;
    }

    private void showError(String msg, Throwable ex) {
        String cause = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        new Alert(Alert.AlertType.ERROR, msg + "\n" + cause, ButtonType.OK).showAndWait();
    }

    private void showErrorAndExit(String msg, Throwable ex) {
        showError(msg, ex);
        System.exit(1);
    }
}