package schedule.assist.demo.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskEditorValidationTest {

    @Test
    void testEmptyString() {
        String res = TaskEditor.validateAndNormalise("");
        assertEquals("Không được để trống", res);
    }

    @Test
    void testInvalidFormat() {
        String res = TaskEditor.validateAndNormalise("abc");
        assertEquals("Định dạng phải là HH:mm (VD: 09:30, 23:59)", res);

        String res2 = TaskEditor.validateAndNormalise("25:00");
        assertEquals("Định dạng phải là HH:mm (VD: 09:30, 23:59)", res2);
    }

    @Test
    void testSingleDigitNormalisation() {
        String res = TaskEditor.validateAndNormalise("9:5");
        assertEquals("09:05", res);
    }

    @Test
    void testValidTime() {
        String res = TaskEditor.validateAndNormalise("09:30");
        assertEquals("09:30", res);

        String res2 = TaskEditor.validateAndNormalise("23:59");
        assertEquals("23:59", res2);
    }
}
