package schedule.assist.demo.model;

public class TaskModel {
    private String titleTask = "Sự kiện";
    private String timeOfTask = "10:00";
    private String placeofTask = "GĐ3";
    private String noteOfTask = "Chuẩn bị laptop";
    private String dayOfWeek = "";

    private double layoutX;
    private double layoutY;

    public TaskModel() {}  // Gson cần cái này để deserialize

    public TaskModel(String title, String time,String place, String note, String dayOfWeek, double x, double y) {
        this.titleTask = title;
        this.timeOfTask = time;
        this.placeofTask = place;
        this.noteOfTask = note;
        this.dayOfWeek = dayOfWeek;
        this.layoutX = x;
        this.layoutY = y;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getTitleTask() {
        return this.titleTask;
    }

    public String getTimeOfTask() {
        return this.timeOfTask;
    }

    public String getPlaceofTask() {
        return placeofTask;
    }

    public String getNoteOfTask() {
        return noteOfTask;
    }

    public double getLayoutX() {
        return this.layoutX;
    }

    public double getLayoutY() {
        return this.layoutY;
    }
}
