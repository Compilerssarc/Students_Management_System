package com.system.studentmanagementsystem;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class Controller {
    @FXML private TextField txtName;
    @FXML private TextField txtCourse;
    @FXML private ChoiceBox<YearLevel> cbYear;
    @FXML private Label lblStatus;
    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colCourse;
    @FXML private TableColumn<Student, String> colYear;
    @FXML private Pagination pagination;

    private ObservableList<Student> allStudentsList = FXCollections.observableArrayList();
    private Connection conn;
    private int selectedId = -1;
    private final int ROWS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        conn = DBConnection.connect();
        cbYear.getItems().setAll(YearLevel.values());

        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colCourse.setCellValueFactory(data -> data.getValue().courseProperty());
        colYear.setCellValueFactory(data -> data.getValue().yearLevelProperty());

        loadData();

        table.setOnMouseClicked(e -> {
            Student s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                selectedId = s.getId();
                txtName.setText(s.getName());
                txtCourse.setText(s.getCourse());
                for (YearLevel y : YearLevel.values()) {
                    if (y.toString().equals(s.getYearLevel())) {
                        cbYear.setValue(y);
                    }
                }
                lblStatus.setText("Selected ID " + selectedId + ". Ready to update or delete.");
                lblStatus.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            }
        });

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> showPage(newIndex.intValue()));
    }

    private void loadData() {
        allStudentsList.clear();
        try {
            String query = "SELECT * FROM students ORDER BY id ASC";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                allStudentsList.add(new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getString("year_level")
                ));
            }

            int pageCount = (int) Math.ceil((double) allStudentsList.size() / ROWS_PER_PAGE);
            pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
            showPage(pagination.getCurrentPageIndex());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allStudentsList.size());

        if (fromIndex > allStudentsList.size() || fromIndex < 0) {
            table.setItems(FXCollections.emptyObservableList());
            return;
        }

        ObservableList<Student> pageItems = FXCollections.observableArrayList(allStudentsList.subList(fromIndex, toIndex));
        table.setItems(pageItems);
    }

    @FXML
    private void addStudent() {
        if (txtName.getText().isEmpty() || txtCourse.getText().isEmpty() || cbYear.getValue() == null) {
            lblStatus.setText("Error: All fields must be filled!");
            lblStatus.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
            return;
        }

        try {
            String findIdQuery = "SELECT COALESCE(" +
                    "  (SELECT MIN(t1.id) + 1 FROM students t1 WHERE NOT EXISTS (SELECT 1 FROM students t2 WHERE t2.id = t1.id + 1))," +
                    "  (SELECT CASE WHEN COUNT(*) = 0 THEN 1 ELSE MAX(id) + 1 END FROM students)," +
                    "  1" +
                    ")";

            int targetId = 1;
            PreparedStatement idPst = conn.prepareStatement(findIdQuery);
            ResultSet rs = idPst.executeQuery();
            if (rs.next()) {
                targetId = rs.getInt(1);
                if (targetId <= 0) targetId = 1;
            }

            String checkOneQuery = "SELECT 1 FROM students WHERE id = 1";
            ResultSet rsOne = conn.createStatement().executeQuery(checkOneQuery);
            if (!rsOne.next()) {
                targetId = 1;
            }

            String query = "INSERT INTO students(id, name, course, year_level) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, targetId);
            pst.setString(2, txtName.getText());
            pst.setString(3, txtCourse.getText());
            pst.setString(4, cbYear.getValue().toString());
            pst.executeUpdate();

            loadData();
            clearFields();
            lblStatus.setText("Student added successfully with ID " + targetId + "!");
            lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void updateStudent() {
        if (selectedId == -1) {
            lblStatus.setText("Help: Click a row inside the table first!");
            lblStatus.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            return;
        }

        try {
            String query = "UPDATE students SET name = ?, course = ?, year_level = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, txtName.getText());
            pst.setString(2, txtCourse.getText());
            pst.setString(3, cbYear.getValue().toString());
            pst.setInt(4, selectedId);
            pst.executeUpdate();

            loadData();
            clearFields();
            lblStatus.setText("Student ID " + selectedId + " updated successfully!");
            lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteStudent() {
        if (selectedId == -1) {
            lblStatus.setText("Help: Click a row inside the table first!");
            lblStatus.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            return;
        }

        try {
            String query = "DELETE FROM students WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, selectedId);
            pst.executeUpdate();

            loadData();
            clearFields();
            lblStatus.setText("Student removed. ID position is now free.");
            lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearFields() {
        txtName.clear();
        txtCourse.clear();
        cbYear.setValue(null);
        selectedId = -1;
        lblStatus.setText("Form cleared. Enter details to add a record.");
        lblStatus.setStyle("-fx-text-fill: #555555;");
    }
}