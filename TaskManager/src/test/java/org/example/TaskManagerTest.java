package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;
    private static final String DB_URL = "jdbc:mysql://localhost:3307/tasks"; // Update this
    private static final String DB_USER = "root"; // Update this
    private static final String DB_PASS = "mysqlTen";


    @BeforeEach
    void setup() {
        taskManager = new TaskManager();

    }

    @Test
    void taskWindowTest() {
        assertNotNull(taskManager);
        assertNotNull(taskManager.Taskwindow());
        assertTrue(taskManager.Taskwindow().isVisible());
    }

    @Test
    void menuViewTest() {
        assertTrue(taskManager.menuView().isVisible());
    }

    @Test
    void viewOptionTest() {
        taskManager.viewMenu.doClick();
        assertTrue(taskManager.homeItem.isVisible());
        assertTrue(taskManager.settingsItem.isVisible());
    }

    @Test
    void taskMenuTest() {
        JMenu taskMenu = taskManager.taskMenu();
        assertNotNull(taskMenu);
        assertEquals(5, taskMenu.getItemCount());
        assertEquals("Add Task", taskMenu.getItem(0).getText());
        assertEquals("Edit Task", taskMenu.getItem(1).getText());
        assertEquals("Delete Task", taskMenu.getItem(2).getText());
        assertEquals(  "Load Data", taskMenu.getItem(3).getText());
        assertEquals(  "Export to PDF", taskMenu.getItem(4).getText());


    }

    @Test
    void fileTaskTest() {
        JMenu fileMenu = taskManager.fileTask();
        assertNotNull(fileMenu);
        assertEquals(1, fileMenu.getItemCount());
        assertEquals("Exit", fileMenu.getItem(0).getText());
    }

//    @Test
//    void addTaskPanel() {
//        JPanel addTaskPanel = taskManager.addTaskPanel();
//        assertNotNull(addTaskPanel);
//        assertTrue(addTaskPanel.isVisible());
//        assertEquals(10, addTaskPanel.getComponentCount());
//
//
//        JButton addTaskButton = (JButton) addTaskPanel.getComponent(9);
//        assertNotNull(addTaskButton);
//        assertEquals("SAVE", addTaskButton.getText());
//
//
//        taskManager.taskField().setText("Test Task");
//        taskManager.taskFieldDescription().setText("Test Description");
//        taskManager.DueTaskDate().setText("2025-02-15");
//        taskManager.taskCheckBox().setSelected(true);
//        addTaskButton.doClick();
//
//            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
//                String taskName = "Test Task";
//
//                // **Step 1: Check if the task exists in DB**
//                String selectSQL = "SELECT * FROM tasks WHERE task_name = ?";
//                try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
//                    selectStmt.setString(1, taskName);
//                    ResultSet resultSet = selectStmt.executeQuery();
//
//                    assertTrue(resultSet.next(), "Task should exist in DB after adding");
//                }
//            } catch (SQLException e) {
//                fail("Database operation failed: " + e.getMessage());
//            }
//
//
//
//
//        assertEquals("Test Task", taskManager.getTaskList().getFirst().getTaskName());
//        assertEquals("Test Description", taskManager.getTaskList().getFirst().getTaskDescription());
//    }

    @Test
    void settingsPanel() {
        JPanel settingsPanel = taskManager.settingsPanel();
        assertNotNull(settingsPanel);
        assertTrue(settingsPanel.isVisible());
        assertEquals(1, settingsPanel.getComponentCount());

        //// toggle button functionality
        JToggleButton toggleButton = (JToggleButton) settingsPanel.getComponent(0);
        assertNotNull(toggleButton);
        assertEquals("DARK MODE", toggleButton.getText());

        toggleButton.doClick();
        assertEquals("LIGHT MODE", toggleButton.getText());
    }

//    @Test
//    void savingAndEditingTaskTest() {
//        JPanel addTaskPanel = taskManager.addTaskPanel();
//        assertNotNull(addTaskPanel);
//        JPanel editPanel = taskManager.editPanel();
//        assertNotNull(editPanel);
//        assertTrue(editPanel.isVisible());
//        assertEquals(2, editPanel.getComponentCount());
//
//        JButton addTaskButton = (JButton) addTaskPanel.getComponent(9);
//        assertNotNull(addTaskButton);
//        assertEquals("SAVE", addTaskButton.getText());
//
//        // Add a task to edit
//        taskManager.taskField().setText("Test Task");
//        taskManager.taskFieldDescription().setText("Test Description");
//        taskManager.DueTaskDate().setText("2023-12-31");
//        taskManager.taskCheckBox().setSelected(true);
//        addTaskButton.doClick();
//        taskManager.taskMenu().getItem(3).doClick();//loading database
//
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
//            String taskName = "Test Task";
//
//            // **Step 1: Check if the task exists in DB**
//            String selectSQL = "SELECT * FROM tasks WHERE task_name = ?";
//            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
//                selectStmt.setString(1, taskName);
//                ResultSet resultSet = selectStmt.executeQuery();
//
//                assertTrue(resultSet.next(), "Task should exist in DB after adding");
//            }
//        } catch (SQLException e) {
//            fail("Database operation failed: " + e.getMessage());
//        }
//
//        // Select the task to edit
//        int lastRow = taskManager.getTaskTable().getRowCount()-1;
//        taskManager.getTaskTable().setRowSelectionInterval(lastRow,lastRow);
//        taskManager.taskMenu().getItem(1).doClick(); // Click Edit Task
//
//        // Test edit functionality
//        JButton confirmEditButton = (JButton) ((JPanel) editPanel.getComponent(1)).getComponent(1);
//        assertNotNull(confirmEditButton);
//        assertEquals("COMFIRM EDIT", confirmEditButton.getText());
//
//        taskManager.editTaskNameField().setText("Updated Task");
//        taskManager.editTaskDescriptionField().setText("Updated Description");
//        taskManager.editDueDate().setText("2024-01-01");
//        taskManager.editTaskCheckBox().setSelected(true);
//        confirmEditButton.doClick();
//        taskManager.taskMenu().getItem(3).doClick();//loading database
//
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
//            String taskName = "Updated Task";
//
//
//            // **Step 1: Check if the task was updated in DB**
//            String selectSQL = "SELECT * FROM tasks WHERE task_name = ?";
//            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
//                selectStmt.setString(1,taskName );
//                ResultSet resultSet = selectStmt.executeQuery();
//
//                assertTrue(resultSet.next(), "Task name should be updated in DB");
//            }
//        } catch (SQLException e) {
//            fail("Database operation failed: " + e.getMessage());
//        }
//
//        assertEquals("Updated Task", taskManager.getTaskList().get(0).getTaskName());
//        assertEquals("Updated Description", taskManager.getTaskList().get(0).getTaskDescription());
//        assertEquals("2024-01-01", taskManager.getTaskList().get(0).getDueDate());
//        assertEquals("COMPLETED", taskManager.getTaskList().get(0).getTaskstatus());
//    }
//    @Test
//    void addingAndDeletingTask(){
//        JPanel addTaskPanel = taskManager.addTaskPanel();
//        assertNotNull(addTaskPanel);
//        assertTrue(addTaskPanel.isVisible());
//        assertEquals(10, addTaskPanel.getComponentCount());
//
//        // add task button functionality
//        JButton addTaskButton = (JButton) addTaskPanel.getComponent(9);
//        assertNotNull(addTaskButton);
//        assertEquals("SAVE", addTaskButton.getText());
//
//        // adding a task
//        taskManager.taskField().setText("Test Task5");
//        taskManager.taskFieldDescription().setText("Test Description");
//        taskManager.DueTaskDate().setText("2023-12-31");
//        taskManager.taskCheckBox().setSelected(true);
//        String taskName ="Test Task5" ;
//        addTaskButton.doClick();
//        taskManager.taskMenu().getItem(3).doClick();//loading database
//
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
//
//
//            // **Step 1: Check if the task exists in DB**
//            String selectSQL = "SELECT * FROM tasks WHERE task_name = ?";
//            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
//                selectStmt.setString(1, taskName);
//                ResultSet resultSet = selectStmt.executeQuery();
//
//                assertTrue(resultSet.next(), "Task should exist in DB after adding");
//            }
//        } catch (SQLException e) {
//            fail("Database operation failed: " + e.getMessage());
//        }
//        int lastRow = taskManager.getTaskTable().getRowCount()-1;
//        //deleting task
//        if(lastRow >=0) {
//            taskManager.getTaskTable().setRowSelectionInterval(0, lastRow);
//            taskManager.taskMenu().getItem(2).doClick();//click Delete Task
//        }
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
//
//        String selectSQL = "SELECT * FROM tasks WHERE task_name = ?";
//        try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
//            selectStmt.setString(1,taskName);
//            ResultSet resultSet = selectStmt.executeQuery();
//
//            assertFalse(resultSet.next(), "Task should NOT exist in DB after deletion");
//        }
//    } catch (SQLException e) {
//        fail("Database operation failed: " + e.getMessage());
//    }
//    }

    @Test
    void exportPdfTest() {
        // Ensure there is at least one row in the table model; add dummy data if necessary.
        DefaultTableModel model = taskManager.getTableModel();
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{1, "Dummy Task", "Dummy Description", "2025-01-01", "Not Completed"});
        }
        // Set a temporary file path for testing PDF export
        String tempFilePath = System.getProperty("java.io.tmpdir") + File.separator + "testExport.pdf";
        // Directly call the exportToPDF(String filePath) method to bypass the file chooser
        taskManager.exportToPDF(tempFilePath);

        // Verify that the PDF file was created
        File exportedFile = new File(tempFilePath);
        assertTrue(exportedFile.exists(), "Exported PDF should exist");

        // Clean up the test file if needed
        exportedFile.delete();
    }

}
