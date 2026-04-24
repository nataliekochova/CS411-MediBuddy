package com.medibuddy.service;

import com.medibuddy.db.Database;
import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;

import java.sql.*;
import java.util.*;

public class MedicationStore {

    private final int userId;
    private final List<SavedMedication> medications = new ArrayList<>();
    private final Map<SavedMedication, Integer> medicationIds = new HashMap<>();
    private final Map<MedicationSchedule, Integer> scheduleIds = new HashMap<>();

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

    public void addMedication(SavedMedication medication) {
        medications.add(medication);
        saveMedication(medication);
    }

    public void removeMedication(SavedMedication medication) {
        medications.remove(medication);

        Integer id = medicationIds.remove(medication);
        if (id == null) {
            return;
        }

        // 🔽 delete schedules FIRST
        String deleteSchedules = "DELETE FROM schedules WHERE medication_id = ?";

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(deleteSchedules)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 🔽 delete medication
        String deleteMedication = "DELETE FROM medications WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(deleteMedication)) {

            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMedications() {
        medications.clear();
        medicationIds.clear();

        String sql = """
                SELECT *
                FROM medications
                WHERE user_id = ?
                ORDER BY id
                """;

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

                // 🔽 LOAD SCHEDULES FOR THIS MED
                loadSchedulesForMedication(med, medId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveMedication(SavedMedication medication) {
        String sql = """
                INSERT INTO medications (
                    user_id,
                    brand_name,
                    generic_name,
                    manufacturer,
                    purpose,
                    indications,
                    warnings,
                    label_dosage,
                    user_dose,
                    user_form
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

    public void setSelectedMedication(SavedMedication med) {
        this.selectedMedication = med;
    }

    public SavedMedication getSelectedMedication() {
        return selectedMedication;
    }

    public void addScheduleToMedication(SavedMedication med, MedicationSchedule schedule) {
        if (med == null) return;

        med.getSchedules().add(schedule);

        Integer medId = medicationIds.get(med);
        if (medId == null) return;

        String sql = """
                INSERT INTO schedules (
                    medication_id,
                    time,
                    frequency_per_day,
                    frequency_type
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, medId);
            stmt.setString(2, schedule.getTime());
            stmt.setInt(3, schedule.getFrequencyPerDay());
            stmt.setString(4, schedule.getFrequencyType());

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

        String sql = "DELETE FROM schedules WHERE id = ?";

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSchedulesForMedication(SavedMedication med, int medId) {
        String sql = """
                SELECT *
                FROM schedules
                WHERE medication_id = ?
                """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MedicationSchedule schedule = new MedicationSchedule(
                        rs.getString("time"),
                        rs.getInt("frequency_per_day"),
                        rs.getString("frequency_type")
                );

                med.getSchedules().add(schedule);
                scheduleIds.put(schedule, rs.getInt("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}