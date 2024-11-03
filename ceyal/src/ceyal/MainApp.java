package ceyal;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.QuadCurveTo;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.text.Text;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.Arrays;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private TableView<EventLog> tableView;
    private ObservableList<EventLog> logData;
    private Pane petriNetPane;
    private ScrollPane scrollPane;
    private Slider zoomSlider;
    private double scaleFactor = 1.0;
    private List<Circle> places;
    private Label totalTimeLabel;
    private double simulationTime = 0;
    private LineChart<Number, Number> performanceChart;
    private VBox processStagesContainer;
    private Map<String, List<EventLog>> stageGroups;
    private double stageGap = 200; // Vertical gap between stages
    private Map<String, Color> stageColors;

    public static void main(String[] args) {
        launch(args);
    }
    
    private void initializeStageColors() {
        stageColors = new HashMap<>();
        stageColors.put("Initiation", Color.LIGHTGREEN);
        stageColors.put("Processing", Color.LIGHTBLUE);
        stageColors.put("Review", Color.LIGHTYELLOW);
        stageColors.put("Completion", Color.LIGHTPINK);
    }
    private void identifyProcessStages() {
        stageGroups = new HashMap<>();
        
        // Define stage patterns (you can customize these based on your event log)
        Map<String, Pattern> stagePatterns = new HashMap<>();
        stagePatterns.put("Initiation", Pattern.compile("(?i).*(start|create|submit|initiate).*"));
        stagePatterns.put("Processing", Pattern.compile("(?i).*(process|handle|work|execute).*"));
        stagePatterns.put("Review", Pattern.compile("(?i).*(review|check|verify|validate).*"));
        stagePatterns.put("Completion", Pattern.compile("(?i).*(complete|finish|end|close).*"));

        // Group events by stages
        for (EventLog event : logData) {
            String eventName = event.getEvent().toLowerCase();
            String matchedStage = stagePatterns.entrySet().stream()
                    .filter(entry -> entry.getValue().matcher(eventName).matches())
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("Processing"); // Default stage

            stageGroups.computeIfAbsent(matchedStage, k -> new ArrayList<>()).add(event);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Process Mining Pro - MVP");

        logData = FXCollections.observableArrayList();
        tableView = new TableView<>(logData);
        initializeTableColumns();

        TextField filterField = createStylizedTextField("Filter by Event, Resource, Cost, or Duration...");
        setupFilterField(filterField);

        Button loadButton = createStylizedButton("Load Event Log", "#4CAF50");
        loadButton.setOnAction(e -> loadEventLog(primaryStage));

        Button visualizeButton = createStylizedButton("Visualize Process", "#2196F3");
        visualizeButton.setOnAction(e -> visualizeProcess());

        Button simulateButton = createStylizedButton("Run Simulation", "#FF5722");
        simulateButton.setOnAction(e -> simulateProcess());

        Button analyzeButton = createStylizedButton("Analyze Log", "#9C27B0");
        analyzeButton.setOnAction(e -> analyzeEventLog());

        Button conformanceButton = createStylizedButton("Conformance Check", "#FFC107");
        conformanceButton.setOnAction(e -> conformanceCheck(new ArrayList<>()));

        Button performanceButton = createStylizedButton("Performance Analysis", "#03A9F4");
        performanceButton.setOnAction(e -> performanceAnalysis());

        Button predictiveButton = createStylizedButton("Predictive Analysis", "#E91E63");
        predictiveButton.setOnAction(e -> predictiveAnalysis());

        zoomSlider = createZoomSlider();
        scrollPane = new ScrollPane();
        petriNetPane = new Pane();
        setupScrollPane();

        VBox leftPanel = createLeftPanel(Arrays.asList(
            loadButton, visualizeButton, simulateButton, analyzeButton, 
            conformanceButton, performanceButton, predictiveButton
        ), zoomSlider, filterField);
        
        SplitPane splitPane = createSplitPane();
        totalTimeLabel = createStylizedLabel("Total Simulation Time: 0.0");

        performanceChart = createPerformanceChart();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createTab("Process Visualization", scrollPane),
            createTab("Performance Chart", performanceChart),
            createTab("Event Log", tableView)
        );

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftPanel);
        mainLayout.setCenter(tabPane);
        mainLayout.setBottom(new HBox(10, totalTimeLabel));
        BorderPane.setMargin(totalTimeLabel, new Insets(10));

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void initializeTableColumns() {
        TableColumn<EventLog, String> eventColumn = new TableColumn<>("Event");
        eventColumn.setCellValueFactory(cellData -> cellData.getValue().eventProperty());

        TableColumn<EventLog, String> timestampColumn = new TableColumn<>("Timestamp");
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());

        TableColumn<EventLog, String> resourceColumn = new TableColumn<>("Resource");
        resourceColumn.setCellValueFactory(cellData -> cellData.getValue().resourceProperty());

        TableColumn<EventLog, String> costColumn = new TableColumn<>("Cost");
        costColumn.setCellValueFactory(cellData -> cellData.getValue().costProperty());

        TableColumn<EventLog, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());

        tableView.getColumns().addAll(eventColumn, timestampColumn, resourceColumn, costColumn, durationColumn);
    }

    private void setupFilterField(TextField filterField) {
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            tableView.setItems(logData.filtered(log ->
                    log.eventProperty().get().toLowerCase().contains(newValue.toLowerCase()) ||
                    log.resourceProperty().get().toLowerCase().contains(newValue.toLowerCase()) ||
                    log.costProperty().get().toLowerCase().contains(newValue.toLowerCase()) ||
                    String.valueOf(log.durationProperty().get()).contains(newValue)));
        });
    }

    private Slider createZoomSlider() {
        Slider slider = new Slider(0.5, 2.0, 1.0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(5);
        slider.setSnapToTicks(true);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scaleFactor = newVal.doubleValue();
            petriNetPane.setScaleX(scaleFactor);
            petriNetPane.setScaleY(scaleFactor);
        });

        return slider;
    }

    private void setupScrollPane() {
        petriNetPane.setMinWidth(800);
        petriNetPane.setMinHeight(1000);
        scrollPane.setContent(petriNetPane);

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double delta = event.getDeltaY() > 0 ? 1.1 : 0.9;
                scaleFactor *= delta;
                petriNetPane.setScaleX(scaleFactor);
                petriNetPane.setScaleY(scaleFactor);
                event.consume();
            }
        });
    }

    private VBox createLeftPanel(List<Button> buttons, Slider slider, TextField filterField) {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #ECEFF1;");
        leftPanel.getChildren().addAll(buttons);
        leftPanel.getChildren().addAll(slider, filterField);
        return leftPanel;
    }

    private SplitPane createSplitPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(tableView, scrollPane);
        splitPane.setDividerPositions(0.3);
        return splitPane;
    }

    private void loadEventLog(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Event Log File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            readEventLog(file);
        }
    }

    private void readEventLog(File file) {
        logData.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip the header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    logData.add(new EventLog(parts[0], parts[1], parts[2], parts[3], parts[4]));
                } else {
                    throw new IOException("Invalid format");
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event log loaded successfully.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error reading the file: " + e.getMessage());
        }
    }

    private void visualizeProcess() {
        petriNetPane.getChildren().clear();
        places = new ArrayList<>();
        initializeStageColors();
        identifyProcessStages();
        drawTopDownProcessModel();
    }

    private void drawTopDownProcessModel() {
        double startX = 100;
        double startY = 50;
        double currentY = startY;
        Map<String, List<Circle>> stageNodes = new HashMap<>();

        // Draw stages and their elements
        for (Map.Entry<String, List<EventLog>> stage : stageGroups.entrySet()) {
            String stageName = stage.getKey();
            List<EventLog> events = stage.getValue();
            
            // Create stage container
            Rectangle stageContainer = createStageContainer(startX - 50, currentY - 30, 
                petriNetPane.getWidth() - 200, events.size() * 100 + 60, stageName);
            petriNetPane.getChildren().add(stageContainer);

            // Draw events within the stage
            List<Circle> stageCircles = new ArrayList<>();
            for (int i = 0; i < events.size(); i++) {
                EventLog event = events.get(i);
                double x = startX + (i * 150);
                Circle eventNode = createEnhancedEventNode(x, currentY + 50, event, stageColors.get(stageName));
                stageCircles.add(eventNode);
                places.add(eventNode);

                // Add interactive elements
                addEventNodeInteractions(eventNode, event);
            }
            
            stageNodes.put(stageName, stageCircles);
            currentY += stageGap;
        }

        // Connect nodes between stages
        connectStages(stageNodes);
    }

    
    private Circle createEnhancedEventNode(double x, double y, EventLog event, Color baseColor) {
        Circle eventNode = new Circle(30);
        
        // Create gradient effect
        Stop[] stops = new Stop[] {
            new Stop(0, baseColor),
            new Stop(1, baseColor.darker())
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null, stops);
        eventNode.setFill(gradient);
        
        eventNode.setCenterX(x);
        eventNode.setCenterY(y);
        eventNode.setEffect(new DropShadow(10, Color.GRAY));

        // Add label
        Text label = new Text(event.getEvent());
        label.setWrappingWidth(100);
        label.setTextAlignment(TextAlignment.CENTER);
        StackPane.setAlignment(label, Pos.CENTER);
        
        // Add to pane
        petriNetPane.getChildren().addAll(eventNode, label);
        label.setLayoutX(x - 50);
        label.setLayoutY(y + 40);

        return eventNode;
    }

    private Rectangle createStageContainer(double x, double y, double width, double height, String stageName) {
        Rectangle container = new Rectangle(x, y, width, height);
        container.setFill(Color.TRANSPARENT);
        container.setStroke(stageColors.get(stageName));
        container.setStrokeWidth(2);
        container.setArcHeight(20);
        container.setArcWidth(20);

        // Add stage title
        Text title = new Text(stageName);
        title.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 16));
        title.setFill(stageColors.get(stageName));
        title.setLayoutX(x + 10);
        title.setLayoutY(y - 10);

        petriNetPane.getChildren().add(title);
        return container;
    }
    
    private void connectStages(Map<String, List<Circle>> stageNodes) {
        String[] stageOrder = {"Initiation", "Processing", "Review", "Completion"};
        
        for (int i = 0; i < stageOrder.length - 1; i++) {
            List<Circle> currentStageNodes = stageNodes.get(stageOrder[i]);
            List<Circle> nextStageNodes = stageNodes.get(stageOrder[i + 1]);
            
            if (currentStageNodes != null && nextStageNodes != null) {
                for (Circle fromNode : currentStageNodes) {
                    Circle toNode = nextStageNodes.get(0); // Connect to first node of next stage
                    drawEnhancedArrow(fromNode, toNode);
                }
            }
        }
    }
    private void drawEnhancedArrow(Circle from, Circle to) {
        double startX = from.getCenterX();
        double startY = from.getCenterY();
        double endX = to.getCenterX();
        double endY = to.getCenterY();

        // Create arrow path
        Path arrow = new Path();
        
        // Calculate control points for curved arrow
        double controlX = (startX + endX) / 2;
        double controlY = (startY + endY) / 2 - 50;

        // Create arrow head
        double arrowLength = 20;
        double arrowWidth = 10;
        
        MoveTo moveTo = new MoveTo(startX, startY);
        QuadCurveTo curve = new QuadCurveTo(controlX, controlY, endX, endY);
        
        arrow.getElements().addAll(moveTo, curve);
        arrow.setStroke(Color.GRAY);
        arrow.setStrokeWidth(2);
        
        // Add arrow to pane
        petriNetPane.getChildren().add(arrow);
    }
    private void addEventNodeInteractions(Circle node, EventLog event) {
        // Hover effect
        node.setOnMouseEntered(e -> {
            node.setEffect(new DropShadow(20, Color.GOLD));
            node.setScaleX(1.2);
            node.setScaleY(1.2);
        });

        node.setOnMouseExited(e -> {
            node.setEffect(new DropShadow(10, Color.GRAY));
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });

        // Click interaction
        node.setOnMouseClicked(e -> {
            showEventDetails(event);
        });
    }
    
    private void showEventDetails(EventLog event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Event Details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Add event details
        grid.add(new Label("Event:"), 0, 0);
        grid.add(new Label(event.getEvent()), 1, 0);
        grid.add(new Label("Resource:"), 0, 1);
        grid.add(new Label(event.getResource()), 1, 1);
        grid.add(new Label("Cost:"), 0, 2);
        grid.add(new Label(event.getCost()), 1, 2);
        grid.add(new Label("Duration:"), 0, 3);
        grid.add(new Label(event.getDuration()), 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    private Circle createGatewayNode(double x, double y) {
        Circle gateway = new Circle(30);
        gateway.setFill(Color.YELLOW); // Use a different color for gateways
        gateway.setCenterX(x);
        gateway.setCenterY(y);
        gateway.setEffect(new DropShadow(10, Color.GRAY));

        gateway.setOnMouseClicked(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Gateway Details", "This is a decision point.");
        });

        petriNetPane.getChildren().add(gateway);
        return gateway;
    }


    private Circle createPlaceNode(double x, double y, EventLog event) {
        Circle place = new Circle(30);
        place.setFill(Color.LIGHTBLUE);
        place.setCenterX(x);
        place.setCenterY(y);
        place.setEffect(new DropShadow(10, Color.GRAY));

        place.setOnMouseClicked(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Event Details",
                    String.format("Event: %s\nResource: %s\nCost: %s\nDuration: %s",
                            event.getEvent(), event.getResource(), event.getCost(), event.getDuration()));
        });

        petriNetPane.getChildren().add(place);
        return place;
    }

    private Rectangle createTransitionNode(double x, double y) {
        Rectangle transition = new Rectangle(80, 30);
        transition.setFill(Color.ORANGE);
        transition.setX(x);
        transition.setY(y);

        transition.setOnMouseClicked(e -> {
            showAlert(Alert.AlertType.INFORMATION, "Transition", "This is a transition.");
        });

        petriNetPane.getChildren().add(transition);
        return transition;
    }

    private void drawArrow(Circle from, Rectangle to) {
        Line line = new Line(from.getCenterX(), from.getCenterY(), to.getX(), to.getY() + 15);
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        petriNetPane.getChildren().add(line);
    }
    private void drawArrow(Circle fromNode, Circle toNode) {
        Line arrowLine = new Line();
        arrowLine.setStartX(fromNode.getCenterX());
        arrowLine.setStartY(fromNode.getCenterY());
        arrowLine.setEndX(toNode.getCenterX());
        arrowLine.setEndY(toNode.getCenterY());

        arrowLine.setStroke(Color.BLACK); // Customize color
        arrowLine.setStrokeWidth(2);

        petriNetPane.getChildren().add(arrowLine);
    }


    private void drawArrow(Rectangle from, Circle to) {
        Line line = new Line(from.getX() + 80, from.getY() + 15, to.getCenterX(), to.getCenterY());
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        petriNetPane.getChildren().add(line);
    }

    private void simulateTokenMovement(Circle from, Rectangle through, Circle to) {
        Circle token = new Circle(10, Color.RED);
        token.setCenterX(from.getCenterX());
        token.setCenterY(from.getCenterY());

        petriNetPane.getChildren().add(token);

        TranslateTransition moveThrough = new TranslateTransition(Duration.seconds(1), token);
        moveThrough.setFromX(from.getCenterX());
        moveThrough.setFromY(from.getCenterY());
        moveThrough.setToX(through.getX() + 40);
        moveThrough.setToY(through.getY() + 15);

        TranslateTransition moveTo = new TranslateTransition(Duration.seconds(1), token);
        moveTo.setFromX(through.getX() + 40);
        moveTo.setFromY(through.getY() + 15);
        moveTo.setToX(to.getCenterX());
        moveTo.setToY(to.getCenterY());

        moveThrough.setOnFinished(e -> moveTo.play());
        moveThrough.play();
    }

    private Circle createActivityNode(double x, double y) {
        Stop[] stops = new Stop[]{new Stop(0, Color.LIGHTBLUE), new Stop(1, Color.DARKBLUE)};
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null, stops);
        Circle circle = new Circle(30);
        circle.setFill(gradient);
        circle.setCenterX(x);
        circle.setCenterY(y);
        circle.setEffect(new DropShadow(10, Color.GRAY));
        petriNetPane.getChildren().add(circle);
        return circle;
    }

    private void drawArrowWithLabel(Circle from, Circle to, double duration, int frequency) {
        Line line = new Line(from.getCenterX(), from.getCenterY(), to.getCenterX(), to.getCenterY());
        line.setStroke(Color.GRAY);
        line.setStrokeWidth(2);
        petriNetPane.getChildren().add(line);

        double midX = (from.getCenterX() + to.getCenterX()) / 2;
        double midY = (from.getCenterY() + to.getCenterY()) / 2;
        Label label = createStylizedLabel(String.format("Duration: %.2f\nFrequency: %d", duration, frequency));
        label.setLayoutX(midX);
        label.setLayoutY(midY);
        petriNetPane.getChildren().add(label);
    }

    private void simulateProcess() {
        createTokenAndAnimate(places.get(0), places.get(places.size() - 1));
        updateTotalSimulationTime();
        showSimulationCompletionMessage();
    }

    private void createTokenAndAnimate(Circle from, Circle to) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(2), new Circle(5, Color.RED));
        transition.setFromX(from.getCenterX());
        transition.setFromY(from.getCenterY());
        transition.setToX(to.getCenterX());
        transition.setToY(to.getCenterY());
        
        petriNetPane.getChildren().add(transition.getNode());
        transition.play();
    }

    private void updateTotalSimulationTime() {
        simulationTime = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.durationProperty().get()))
                .sum();
        totalTimeLabel.setText(String.format("Total Simulation Time: %.2f", simulationTime));
    }

    private void showSimulationCompletionMessage() {
        showAlert(Alert.AlertType.INFORMATION, "Simulation", "Simulation completed successfully.");
    }


    private void analyzeEventLog() {
        int totalEvents = logData.size();
        double totalCost = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.costProperty().get()))
                .sum();

        double totalDuration = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.durationProperty().get()))
                .sum();

        String analysisResult = String.format(
            "Total Events: %d\nTotal Cost: $%.2f\nTotal Duration: %.2f units",
            totalEvents, totalCost, totalDuration
        );

        showAlert(Alert.AlertType.INFORMATION, "Event Log Analysis", analysisResult);
    }

    private void conformanceCheck(List<String> predefinedModel) {
        List<String> actualModel = logData.stream()
                .map(EventLog::getEvent)
                .collect(Collectors.toList());

        List<String> missingSteps = new ArrayList<>(predefinedModel);
        missingSteps.removeAll(actualModel);

        List<String> extraSteps = new ArrayList<>(actualModel);
        extraSteps.removeAll(predefinedModel);

        String conformanceResult = String.format(
            "Missing Steps: %s\nExtra Steps: %s",
            String.join(", ", missingSteps),
            String.join(", ", extraSteps)
        );

        showAlert(Alert.AlertType.INFORMATION, "Conformance Check Results", conformanceResult);
    }

    private void performanceAnalysis() {
        double averageTime = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.getDuration()))
                .average().orElse(0);

        double averageCost = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.getCost()))
                .average().orElse(0);

        String performanceResult = String.format(
            "Average Time: %.2f units\nAverage Cost: $%.2f",
            averageTime, averageCost
        );

        showAlert(Alert.AlertType.INFORMATION, "Performance Analysis", performanceResult);
        updatePerformanceChart();
    }

    private void predictiveAnalysis() {
        double averageTime = logData.stream()
                .mapToDouble(log -> Double.parseDouble(log.getDuration()))
                .average().orElse(0);

        List<EventLog> predictedDelays = logData.stream()
                .filter(log -> Double.parseDouble(log.getDuration()) > averageTime)
                .collect(Collectors.toList());

        String delays = predictedDelays.stream()
                .map(log -> String.format("%s (Duration: %s)", log.getEvent(), log.getDuration()))
                .collect(Collectors.joining("\n"));

        String predictionResult = "Predicted Delays:\n" + delays;
        showAlert(Alert.AlertType.INFORMATION, "Predictive Analysis", predictionResult);
    }

    private LineChart<Number, Number> createPerformanceChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Event Index");
        yAxis.setLabel("Duration");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Event Duration Performance");
        return lineChart;
    }

    private void updatePerformanceChart() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Event Duration");

        for (int i = 0; i < logData.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, Double.parseDouble(logData.get(i).getDuration())));
        }

        performanceChart.getData().clear();
        performanceChart.getData().add(series);
    }

    private Button createStylizedButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px;", color));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    private TextField createStylizedTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle("-fx-font-size: 14px;");
        return textField;
    }

    private Label createStylizedLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    private Tab createTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

