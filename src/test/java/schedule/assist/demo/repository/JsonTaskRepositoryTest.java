package schedule.assist.demo.repository;

import org.junit.jupiter.api.Test;
import schedule.assist.demo.model.TaskModel;
import schedule.assist.demo.util.DataManager;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTaskRepositoryTest {
    @Test
    void testDelegatesToDataManager() {
        JsonTaskRepository repo = new JsonTaskRepository();
        List<TaskModel> empty = Collections.emptyList();
        repo.saveAll(empty);

        List<TaskModel> loaded = repo.loadAll();
        assertEquals(0, loaded.size());
    }
}
