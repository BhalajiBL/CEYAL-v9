private void drawBPMNProcessModel() {
    double startX = 100;
    double startY = 50;
    double currentY = startY;

    for (Map.Entry<String, List<EventLog>> stage : stageGroups.entrySet()) {
        String stageName = stage.getKey();
        List<EventLog> events = stage.getValue();

        Text title = new Text(stageName);
        title.setFont(javafx.scene.text.Font.font("Arial", FontWeight.BOLD, 16));
        title.setFill(stageColors.get(stageName));
        title.setLayoutX(startX + 10);
        title.setLayoutY(currentY - 10);
        petriNetPane.getChildren().add(title);

        for (int i = 0; i < events.size(); i++) {
            EventLog event = events.get(i);
            double x = startX + (i * 150);

            // Check if it's a start or end event based on the event name
            if (event.getEvent().toLowerCase().contains("start")) {
                createStartEventNode(x, currentY);
            } else if (event.getEvent().toLowerCase().contains("end")) {
                createEndEventNode(x, currentY);
            } else {
                createTaskNode(x, currentY, event);
            }
        }
        
        currentY += stageGap;
    }
}
