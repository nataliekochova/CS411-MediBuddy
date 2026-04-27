package com.medibuddy.service;

import com.medibuddy.db.Database;
import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class MedicationStore {

    private final int userId;

    private final List<SavedMedication> medications = new ArrayList<>();
    private final Map<SavedMedication, Integer> medicationIds = new HashMap<>();
    private final Map<MedicationSchedule, Integer> scheduleIds = new HashMap<>();
    private final Map<String, Boolean> adherenceCache = new HashMap<>();

    private SavedMedication selectedMedication;

    public MedicationStore(int userId) {
        this.userId = userId;
        loadMedications();
    }

    public int getUserId() {
        return userId;
    }

    public List<SavedMedication> getMedications() {
        return Collections.unmodifiableList(medications);
    }

    public List<SavedMedication> getAllMedications() {
        return medications;
    }

    // -----------------------------
    // LOAD MEDS + SCHEDULES
    // -----------------------------

    private void loadMedications() {
        medications.clear();
        medicationIds.clear();

        String sql = "SELECT * FROM medications WHERE user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SavedMedication med = new SavedMedication(
                        rs.getString("brand_name"),
                        rs.getString("generic_name"),
                        rs.getString("manufacturer"),
                        rs.getString("purpose"),
                        rs.getString("indications"),
                        rs.getString("warnings"),
                        rs.getString("label_dosage"),
                        rs.getString("user_dose"),
                        rs.getString("user_form")
                );

                int medId = rs.getInt("id");

                medications.add(med);
                medicationIds.put(med, medId);

                loadSchedulesForMedication(med, medId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSchedulesForMedication(SavedMedication med, int medId) {
        String sql = "SELECT * FROM schedules WHERE medication_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MedicationSchedule schedule = new MedicationSchedule(
                        rs.getString("day"),
                        rs.getString("time")
                );

                med.addSchedule(schedule);
                scheduleIds.put(schedule, rs.getInt("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------
    // ADD / REMOVE MEDICATIONS
    // -----------------------------

    public void addMedication(SavedMedication medication) {
        medications.add(medication);

        String sql = """
                INSERT INTO medications (
                    user_id, brand_name, generic_name, manufacturer,
                    purpose, indications, warnings, label_dosage,
                    user_dose, user_form
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setString(2, medication.getBrandName());
            stmt.setString(3, medication.getGenericName());
            stmt.setString(4, medication.getManufacturer());
            stmt.setString(5, medication.getPurpose());
            stmt.setString(6, medication.getIndications());
            stmt.setString(7, medication.getWarnings());
            stmt.setString(8, medication.getLabelDosage());
            stmt.setString(9, medication.getUserDose());
            stmt.setString(10, medication.getUserForm());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                medicationIds.put(medication, keys.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMedication(SavedMedication medication) {
        medications.remove(medication);

        Integer id = medicationIds.remove(medication);
        if (id == null) return;

        try (Connection conn = Database.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM schedules WHERE medication_id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM medications WHERE id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------
    // SCHEDULE MANAGEMENT
    // -----------------------------

    public void addScheduleToMedication(SavedMedication med, MedicationSchedule schedule) {
        if (med == null) return;

        med.addSchedule(schedule);

        Integer medId = medicationIds.get(med);
        if (medId == null) return;

        String sql = """
                INSERT INTO schedules (medication_id, day, time)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, medId);
            stmt.setString(2, schedule.getDay());
            stmt.setString(3, schedule.getTime());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                scheduleIds.put(schedule, keys.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeScheduleFromMedication(SavedMedication med, MedicationSchedule schedule) {
        if (med == null) return;

        med.getSchedules().remove(schedule);

        Integer id = scheduleIds.remove(schedule);
        if (id == null) return;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM schedules WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String key(int scheduleId, String date) {
        return scheduleId + "|" + date;
    }

    public Boolean getDoseStatus(LocalDate date, MedicationSchedule sched) {
        Integer schedId = scheduleIds.get(sched);
        if (schedId == null) return null;

        String dateStr = date.toString();
        String k = key(schedId, dateStr);

        if (adherenceCache.containsKey(k)) {
            return adherenceCache.get(k);
        }

        String sql = """
                SELECT taken FROM adherence
                WHERE user_id = ? AND schedule_id = ? AND date = ?
                """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, schedId);
            stmt.setString(3, dateStr);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean val = rs.getInt("taken") == 1;
                adherenceCache.put(k, val);
                return val;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setDoseStatus(LocalDate date, MedicationSchedule sched, Boolean status) {
        Integer schedId = scheduleIds.get(sched);
        if (schedId == null) return;

        String dateStr = date.toString();
        String k = key(schedId, dateStr);

        adherenceCache.put(k, status);

        String deleteSql = """
                DELETE FROM adherence
                WHERE user_id = ? AND schedule_id = ? AND date = ?
                """;

        String insertSql = """
                INSERT INTO adherence (user_id, schedule_id, date, taken)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, schedId);
                stmt.setString(3, dateStr);
                stmt.executeUpdate();
            }

            if (status != null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, schedId);
                    stmt.setString(3, dateStr);
                    stmt.setInt(4, status ? 1 : 0);
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------

    public void setSelectedMedication(SavedMedication med) {
        this.selectedMedication = med;
    }

    public SavedMedication getSelectedMedication() {
        return selectedMedication;
    }
}