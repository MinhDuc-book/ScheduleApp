package schedule.assist.demo.service;

import schedule.assist.demo.ui.Task;
import schedule.assist.demo.model.TaskModel;

import java.util.List;

public interface TaskService {
    Task createTask();
    void loadTask(TaskModel model);
    void deleteTask(Task task);
    void saveAll();
    List<Task> getAll();
}
