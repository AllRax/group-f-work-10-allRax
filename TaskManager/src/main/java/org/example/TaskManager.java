package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class TaskManager {
    JFrame frame;
    JMenu filemenu, viewMenu;
    JMenuItem homeItem, settingsItem;
    private JPanel northPanel, southPanel, westPanel, centerPanel, listPanel, taskPanel;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JTextField taskField, taskFieldDescription, editdueDate, dueDate, editTaskNameLabel, editTaskDescriptionField;
    private JToggleButton toggleButton = new JToggleButton("DARK MODE");
    private CardLayout cardLayout = new CardLayout();
    private ArrayList<Tasks> taskList = new ArrayList<>();
    private JCheckBox taskCheckBox, edit_checkBox;
    private static final String DB_URL = "jdbc:mysql://localhost:3307/tasks";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public TaskManager() {
        this.Taskwindow();
    }

    public JFrame Taskwindow() {
        frame = new JFrame();
        frame.setTitle("TODO LIST");
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(this.menuView());
        frame.setLayout(new BorderLayout());
        frame.add(BorderLayout.NORTH, this.north());
        frame.add(BorderLayout.EAST, this.nullPanel());
        frame.add(BorderLayout.WEST, this.west());
        frame.add(BorderLayout.SOUTH, this.south());
        frame.add(BorderLayout.CENTER, this.center());
        frame.setMinimumSize(new Dimension(400, 300));
        frame.pack();
        return frame;
    }

    // Menu Bar consisting of the nav components
    public JMenuBar menuView() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(this.viewOption());
        menuBar.add(this.taskMenu());
        menuBar.add(this.fileTask());
        menuBar.setBackground(new Color(176, 196, 222));
        return menuBar;
    }

    // View Option on task menuBar
    public JMenu viewOption() {
        viewMenu = new JMenu("View");
        homeItem = new JMenuItem("home");
        settingsItem = new JMenuItem("settings");
        viewMenu.add(homeItem);
        viewMenu.add(settingsItem);
        homeItem.addActionListener(e -> cardLayout.show(centerPanel, "HOME"));
        settingsItem.addActionListener(e -> cardLayout.show(centerPanel, "SETTINGS"));
        return viewMenu;
    }

    // Content Panel
    public JPanel center() {
        centerPanel = new JPanel(cardLayout);
        centerPanel.setPreferredSize(new Dimension(100, 100));
        centerPanel.setForeground(Color.white);
        centerPanel.setBackground(Color.BLUE);
        // Replace the JList panel with our JTable panel
        centerPanel.add(this.taskTablePanel(), "HOME");
        centerPanel.add(this.addTaskPanel(), "ADDTASK");
        centerPanel.add(this.settingsPanel(), "SETTINGS");
        centerPanel.add(this.editPanel(), "EDIT");
        return centerPanel;
    }

    // Task Menu Option on task menuBar with new buttons added
    public JMenu taskMenu() {
        JMenu taskMenu = new JMenu("Task Menu");
        JMenuItem addTask = new JMenuItem("Add Task");
        JMenuItem editTask = new JMenuItem("Edit Task");
        JMenuItem deleteTask = new JMenuItem("Delete Task");
        // NEW items:
        JMenuItem loadData = new JMenuItem("Load Data");
        JMenuItem exportPdf = new JMenuItem("Export to PDF");
        taskMenu.add(addTask);
        taskMenu.add(editTask);
        taskMenu.add(deleteTask);
        taskMenu.add(loadData);
        taskMenu.add(exportPdf);

        addTask.addActionListener(e -> cardLayout.show(centerPanel, "ADDTASK"));

        editTask.addActionListener(e -> {
            int selected = taskTable.getSelectedRow();
            if (selected >= 0 && selected < tableModel.getRowCount()) {
                Tasks selectedTask = taskList.get(selected);
                editdueDate.setText(selectedTask.getDueDate());
                editTaskDescriptionField.setText(selectedTask.getTaskDescription());
                editTaskNameLabel.setText(selectedTask.getTaskName());
                edit_checkBox.setSelected(false);
            }
            cardLayout.show(centerPanel, "EDIT");
        });

        deleteTask.addActionListener(e -> {
            int selected = taskTable.getSelectedRow();
            if (selected >= 0 && selected < tableModel.getRowCount()) {
                int taskId = (int) tableModel.getValueAt(selected, 0);
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
                    stmt.setInt(1, taskId);
                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(deleteTask, "Task deleted successfully!");
                        taskList.remove(selected);
                        tableModel.removeRow(selected);
                    } else {
                        JOptionPane.showMessageDialog(deleteTask, "Task not found in database.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(deleteTask, "Error deleting task: " + ex.getMessage());
                }
            }
        });
        loadData.addActionListener(e -> loadTasksFromDatabase());
        exportPdf.addActionListener(e -> exportToPDF());

        return taskMenu;
    }

    // File Option on the task menuBar
    public JMenu fileTask() {
        filemenu = new JMenu("file");
        JMenuItem exitItem = new JMenuItem("Exit");
        filemenu.add(exitItem);
        exitItem.addActionListener(e -> System.exit(0));
        return filemenu;
    }

    // northPanel
    public JPanel north() {
        northPanel = new JPanel(new BorderLayout());
        northPanel.setPreferredSize(new Dimension(100, 250));
        return northPanel;
    }

    // southPanel
    public JPanel south() {
        southPanel = new JPanel();
        southPanel.setPreferredSize(new Dimension(100, 200));
        return southPanel;
    }

    // west Panel
    public JPanel west() {
        westPanel = new JPanel();
        westPanel.setPreferredSize(new Dimension(250, 100));
        return westPanel;
    }

    // Dummy right panel
    public JPanel nullPanel() {
        JPanel right = new JPanel();
        right.setPreferredSize(new Dimension(200, 100));
        return right;
    }

    // JTable panel
    public JPanel taskTablePanel() {
        listPanel = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"ID", "Task Name", "Task Description", "Due Date", "Task Status"}, 0);
        taskTable = new JTable(tableModel);
        loadTasksFromDatabase();
        JScrollPane scrollPane = new JScrollPane(taskTable);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        return listPanel;
    }

    public JPanel addTaskPanel() {
        taskPanel = new JPanel();
        taskPanel.setPreferredSize(new Dimension(100, 100));
        GridLayout gridLayout = new GridLayout(5, 2);
        gridLayout.setHgap(10);
        gridLayout.setVgap(10);
        taskPanel.setLayout(gridLayout);
        taskPanel.add(this.taskLabel());
        taskPanel.add(this.taskField());
        taskPanel.add(this.taskLabelDescription());
        taskPanel.add(this.taskFieldDescription());
        taskPanel.add(this.DueTaskLabel());
        taskPanel.add(this.DueTaskDate());
        taskPanel.add(this.taskStatusLabel());
        taskPanel.add(this.taskCheckBox());
        taskPanel.add(this.CancelEdit());
        taskPanel.add(this.addTaskButton());
        return taskPanel;
    }

    public ArrayList<Tasks> getTaskList() {
        return taskList;
    }

    public JButton addTaskButton() {
        JButton saveTask = new JButton("SAVE");
        saveTask.setForeground(Color.WHITE);
        saveTask.setBackground(new Color(30, 144, 255));

        saveTask.addActionListener(e->{
            String taskName=taskField.getText();
            String taskDescription=taskFieldDescription.getText();
            String dueTaskDate=dueDate.getText();
            String status=taskCheckBox.isSelected() ? "Completed" : "Not Completed";
            if (!taskName.isBlank() && !taskDescription.isBlank() && !dueTaskDate.isBlank()) {
                Tasks newTask = new Tasks(taskName, taskDescription, dueTaskDate, status);
                taskField.setText("");
                taskFieldDescription.setText("");
                dueDate.setText("");
                taskCheckBox.setSelected(false);
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO tasks (task_name, task_description, due_date, task_status) VALUES (?, ?, ?, ?)")) {

                    stmt.setString(1, taskName);
                    stmt.setString(2, taskDescription);
                    stmt.setString(3, dueTaskDate);
                    stmt.setString(4, status);

                    int rowsInserted = stmt.executeUpdate();
                    if (rowsInserted > 0) {
                        JOptionPane.showMessageDialog(addTaskButton(), "Task saved successfully!");
                        taskList.add(newTask);
                        tableModel.addRow(new Object[]{taskList.size(), taskName, taskDescription, dueTaskDate, status});
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(addTaskPanel(), "Error saving task: " + ex.getMessage());
                }
            }
            cardLayout.show(centerPanel, "HOME");
        });
        return saveTask;
    }

    // Task Name label
    public JLabel taskLabel() {
        JLabel taskName = new JLabel();
        taskName.setText("TASK NAME: ");
        taskName.setFont(new java.awt.Font("ARIAL", Font.BOLD, 14));
        return taskName;
    }

    public JTextField taskField() {
        taskField = new JTextField();
        return taskField;
    }

    public JLabel taskLabelDescription() {
        JLabel taskName = new JLabel();
        taskName.setText("TASK DESCRIPTION: ");
        taskName.setFont(new java.awt.Font("ARIAL", Font.BOLD, 14));
        return taskName;
    }

    public JTextField taskFieldDescription() {
        taskFieldDescription = new JTextField();
        return taskFieldDescription;
    }

    public JLabel taskStatusLabel() {
        JLabel statusLabel = new JLabel("TASK STATUS: ");
        statusLabel.setFont(new java.awt.Font("ARIAL", Font.BOLD, 14));
        return statusLabel;
    }

    public JCheckBox taskCheckBox() {
        taskCheckBox = new JCheckBox();
        return taskCheckBox;
    }

    public JLabel DueTaskLabel() {
        JLabel dueDate = new JLabel("DUE DATE:");
        dueDate.setFont(new java.awt.Font("ARIAL", Font.BOLD, 14));
        return dueDate;
    }

    public JTextField DueTaskDate() {
        dueDate = new JTextField();
        return dueDate;
    }

    public JTextField editDueDate() {
        editdueDate = new JTextField();
        return editdueDate;
    }

    public JPanel settingsPanel() {
        JPanel settingsPanel = new JPanel(new BorderLayout());
        toggleButton.addItemListener(itemListener);
        settingsPanel.add(BorderLayout.CENTER, toggleButton);
        settingsPanel.setBackground(Color.DARK_GRAY);
        return settingsPanel;
    }

    public JPanel editPanel() {
        JPanel editPanel = new JPanel(new BorderLayout());
        editPanel.add(BorderLayout.CENTER, this.upperside());
        editPanel.add(BorderLayout.SOUTH, this.downSide());
        return editPanel;
    }

    public JPanel upperside() {
        JPanel upSide = new JPanel();
        upSide.setPreferredSize(new Dimension(100, 100));
        GridLayout gridLayout = new GridLayout(4, 2);
        gridLayout.setHgap(10);
        gridLayout.setVgap(10);
        upSide.setLayout(gridLayout);
        upSide.add(this.taskLabel());
        upSide.add(this.editTaskNameField());
        upSide.add(this.taskLabelDescription());
        upSide.add(this.editTaskDescriptionField());
        upSide.add(this.DueTaskLabel());
        upSide.add(this.editDueDate());
        upSide.add(this.taskStatusLabel());
        upSide.add(this.editTaskCheckBox());
        return upSide;
    }

    public JPanel downSide() {
        JPanel down = new JPanel();
        down.add(this.CancelEdit());
        down.add(this.comfirmEdit());
        return down;
    }

    public JButton comfirmEdit() {
        JButton comfirmEdit = new JButton("COMFIRM EDIT");
        comfirmEdit.setBackground(new Color(30, 144, 255));
        comfirmEdit.setForeground(Color.WHITE);
        comfirmEdit.addActionListener(e -> {
            int selectedIndex = taskTable.getSelectedRow();
            if (selectedIndex >= 0 && selectedIndex < tableModel.getRowCount()) {
                Tasks selectedTask = taskList.get(selectedIndex);
                String oldName = selectedTask.getTaskName();
                selectedTask.setTaskName(editTaskNameLabel.getText());
                selectedTask.setTaskDescription(editTaskDescriptionField.getText());
                selectedTask.setDueDate(editdueDate.getText());
                taskCheckBox.setSelected(false);
                taskList.set(selectedIndex, new Tasks(editTaskNameLabel.getText(), editTaskDescriptionField.getText(), editdueDate.getText(), edit_checkBox.isSelected() ? "COMPLETED" : "NOT COMPLETED"));
                tableModel.setValueAt(selectedTask.getTaskName(), selectedIndex, 1);
                tableModel.setValueAt(selectedTask.getTaskDescription(), selectedIndex, 2);
                tableModel.setValueAt(selectedTask.getDueDate(), selectedIndex, 3);
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     PreparedStatement stmt = conn.prepareStatement("UPDATE tasks SET task_name = ?, task_description = ?, due_date = ? WHERE task_name = ?")) {
                    stmt.setString(1, editTaskNameLabel.getText());
                    stmt.setString(2, editTaskDescriptionField.getText());
                    stmt.setString(3, editdueDate.getText());
                    stmt.setString(4, oldName);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(comfirmEdit, "Task updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(comfirmEdit, "Task not found in database.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(comfirmEdit, "Error updating task: " + ex.getMessage());
                }
                cardLayout.show(centerPanel, "HOME");
                editTaskDescriptionField.setText("");
                editTaskNameLabel.setText("");
                editdueDate.setText("");
            }
        });
        return comfirmEdit;
    }

    public JButton CancelEdit() {
        JButton cancelEdit = new JButton("CANCEL");
        cancelEdit.setForeground(Color.WHITE);
        cancelEdit.setBackground(new Color(139, 0, 0));
        cancelEdit.addActionListener(e -> {
            editTaskDescriptionField.setText("");
            editTaskNameLabel.setText("");
            editdueDate.setText("");
            cardLayout.show(centerPanel, "HOME");
        });
        return cancelEdit;
    }

    public JTextField editTaskNameField() {
        editTaskNameLabel = new JTextField();
        return editTaskNameLabel;
    }

    public JTextField editTaskDescriptionField() {
        editTaskDescriptionField = new JTextField();
        return editTaskDescriptionField;
    }

    public JCheckBox editTaskCheckBox() {
        edit_checkBox = new JCheckBox();
        return edit_checkBox;
    }

    // ItemListener for the dark mode toggle button
    ItemListener itemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent itemEvent) {
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED) {
                toggleButton.setText("LIGHT MODE");
                toggleButton.setBackground(Color.BLACK);
                toggleButton.setForeground(Color.WHITE);
                System.out.println("Selected");
            } else {
                toggleButton.setText("DARK MODE");
                toggleButton.setBackground(Color.WHITE);
                toggleButton.setForeground(Color.BLACK);
                System.out.println("Deselected");
            }
        }
    };

    // NEW: Load tasks from the database into the JTable
    private void loadTasksFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tasks")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("task_name"));
                row.add(rs.getString("task_description"));
                row.add(rs.getString("due_date"));
                row.add(rs.getString("task_status"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading tasks: " + e.getMessage());
        }
    }

    // NEW: Export the JTable data to a PDF report using iText
    private void exportToPDF() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "No data to export.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF");
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath() + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                Document document = new Document();
                PdfWriter.getInstance(document, fos);
                document.open();
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
                Paragraph title = new Paragraph("TASKS TABLE", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("\n"));
                Paragraph timestamp = new Paragraph("Report generated on: " + new java.util.Date());
                timestamp.setAlignment(Element.ALIGN_CENTER);
                document.add(timestamp);
                document.add(new Paragraph("\n"));
                PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount());
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(tableModel.getColumnName(i), titleFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    pdfTable.addCell(cell);
                }
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        pdfTable.addCell(tableModel.getValueAt(i, j).toString());
                    }
                }
                document.add(pdfTable);
                document.close();
                JOptionPane.showMessageDialog(frame, "PDF exported successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error exporting PDF: " + e.getMessage());
            }
        }
    }
}
