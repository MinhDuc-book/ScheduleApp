package schedule.assist.demo.repository;

import schedule.assist.demo.model.TaskModel;

import java.util.List;

public interface TaskRepository {
    List<TaskModel> loadAll();

    void saveAll(List<TaskModel> tasks);
}
