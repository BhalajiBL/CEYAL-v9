package ceyal;

import javafx.beans.property.SimpleStringProperty;

class EventLog {
    private final SimpleStringProperty event;
    private final SimpleStringProperty timestamp;
    private final SimpleStringProperty resource;
    private final SimpleStringProperty cost;
    private final SimpleStringProperty duration;

    public EventLog(String event, String timestamp, String resource, String cost, String duration) {
        this.event = new SimpleStringProperty(event);
        this.timestamp = new SimpleStringProperty(timestamp);
        this.resource = new SimpleStringProperty(resource);
        this.cost = new SimpleStringProperty(cost);
        this.duration = new SimpleStringProperty(duration);
    }

    public String getEvent() { return event.get(); }
    public void setEvent(String event) { this.event.set(event); }
    public SimpleStringProperty eventProperty() { return event; }

    public String getTimestamp() { return timestamp.get(); }
    public void setTimestamp(String timestamp) { this.timestamp.set(timestamp); }
    public SimpleStringProperty timestampProperty() { return timestamp; }

    public String getResource() { return resource.get(); }
    public void setResource(String resource) { this.resource.set(resource); }
    public SimpleStringProperty resourceProperty() { return resource; }

    public String getCost() { return cost.get(); }
    public void setCost(String cost) { this.cost.set(cost); }
    public SimpleStringProperty costProperty() { return cost; }

    public String getDuration() { return duration.get(); }
    public void setDuration(String duration) { this.duration.set(duration); }
    public SimpleStringProperty durationProperty() { return duration; }
}