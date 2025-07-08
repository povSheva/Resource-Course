package com.example.demo.ui;

import com.example.demo.domain.Resource;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ResourceManagerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Top: search bar + export button
        TextField searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setMaxWidth(300);

        Button exportBtn = new Button("Export");
        HBox topBar = new HBox(10, searchField, exportBtn);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        root.setTop(topBar);

        // Left: pinned, add button, filters
        Label pinnedLabel = new Label("Pinned");
        ListView<String> pinnedList = new ListView<>(
                FXCollections.observableArrayList("course-outline.pdf", "article-link")
        );
        pinnedList.setMaxHeight(100);

        Button addBtn = new Button("+ Добавить ресурс");
        addBtn.setMaxWidth(Double.MAX_VALUE);

        Label filterLabel = new Label("Filter");
        ListView<String> filterList = new ListView<>(
                FXCollections.observableArrayList("All", "PDF", "Images", "Links")
        );
        filterList.setMaxHeight(120);

        VBox leftBar = new VBox(10,
                pinnedLabel, pinnedList,
                addBtn,
                filterLabel, filterList
        );
        leftBar.setPadding(new Insets(10));
        leftBar.setPrefWidth(200);
        root.setLeft(leftBar);

        // Center: table + status
        TableView<Resource> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Resource, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Resource res = getTableView().getItems().get(getIndex());
                    Label icon = new Label();
                    switch (res.getType()) {
                        case "PDF":
                            icon.setText("📄");
                            break;
                        case "Image":
                            icon.setText("🖼️");
                            break;
                        case "Link":
                            icon.setText("🔗");
                            break;
                    }
                    Label text = new Label(item);
                    HBox hb = new HBox(5, icon, text);
                    setGraphic(hb);
                }
            }
        });

        TableColumn<Resource, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Resource, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(nameCol, typeCol, dateCol);

        ObservableList<Resource> data = FXCollections.observableArrayList(
                new Resource("Course Outline", "PDF", "04.01.2024"),
                new Resource("Landscape Photo", "Image", "03.01.2024"),
                new Resource("Course Information PDF", "PDF", "02.01.2024"),
                new Resource("Image File", "Image", "02.01.2024"),
                new Resource("Introduction to Course", "Link", "31.12.2023")
        );
        table.setItems(data);

        Label statusLabel = new Label(data.size() + " записей");

        VBox centerBox = new VBox(5, table, statusLabel);
        centerBox.setPadding(new Insets(10));
        root.setCenter(centerBox);

        // Right: preview panel
        Label title = new Label("Course Outline");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label desc = new Label(
                "An overview of the course content, including topics, objectives, and evaluation methods."
        );
        desc.setWrapText(true);

        Separator sep = new Separator();

        Button downloadBtn = new Button("⭳");  // стрелка вниз
        Button pinBtn = new Button("🔖");  // закладка
        HBox actions = new HBox(10, downloadBtn, pinBtn);
        actions.setAlignment(Pos.CENTER);

        VBox rightBox = new VBox(10, title, desc, sep, actions);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(250);
        root.setRight(rightBox);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Resource Manager");
        primaryStage.show();
    }
}
