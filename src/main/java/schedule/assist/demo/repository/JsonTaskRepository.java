package schedule.assist.demo.repository;

import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.util.DataManager;

import java.util.List;

public class JsonTaskRepository implements TaskRepository {

    @Override
    public List<TaskModel> loadAll() {
        return DataManager.loadTasks();
    }

    @Override
    public void saveAll(List<TaskModel> tasks) {
        DataManager.saveTasks(tasks);
    }
}
