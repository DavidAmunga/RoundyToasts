package miles.identigate.soja.models;

public class Notification {

    public String title;
    public String body;
    public String data;

    public Notification(String title, String body, String data) {
        this.title = title;
        this.body = body;
        this.data = data;
    }

    public Notification() {
    }

    public String getTitle() {
        return title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
