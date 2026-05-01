package com.medibuddy.service;

import com.medibuddy.db.Database;
import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.model.EmergencyContact;

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
        scheduleIds.clear();
        adherenceCache.clear();

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

                String startDate = rs.getString("start_date");
                if (startDate != null && !startDate.isBlank()) {
                    med.setStartDate(LocalDate.parse(startDate));
                }

                String endDate = rs.getString("end_date");
                if (endDate != null && !endDate.isBlank()) {
                    med.setEndDate(LocalDate.parse(endDate));
                }

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
                int contactId = rs.getInt("emergency_contact_id");
                Integer emergencyContactId = rs.wasNull() ? null : contactId;
                String day = rs.getString("day");
                String time = rs.getString("time");

                if (day == null || day.isBlank() || time == null || time.isBlank()) {
                    continue;
                }

                MedicationSchedule schedule = new MedicationSchedule(
                        day,
                        time,
                        rs.getInt("critical_alert_enabled") == 1,
                        emergencyContactId,
                        rs.getInt("missed_window_minutes") == 0 ? 30 : rs.getInt("missed_window_minutes")
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
                    user_dose, user_form, start_date, end_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            stmt.setString(11, medication.getStartDate() == null ? null : medication.getStartDate().toString());
            stmt.setString(12, medication.getEndDate() == null ? null : medication.getEndDate().toString());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                medicationIds.put(medication, keys.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMedicationDateRange(SavedMedication medication) {
        Integer medId = medicationIds.get(medication);
        if (medId == null) return;

        String sql = """
                UPDATE medications
                SET start_date = ?, end_date = ?
                WHERE id = ? AND user_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medication.getStartDate() == null ? null : medication.getStartDate().toString());
            stmt.setString(2, medication.getEndDate() == null ? null : medication.getEndDate().toString());
            stmt.setInt(3, medId);
            stmt.setInt(4, userId);
            stmt.executeUpdate();

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
                INSERT INTO schedules (
                    medication_id,
                    day,
                    time,
                    critical_alert_enabled,
                    emergency_contact_id,
                    missed_window_minutes
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, medId);
            stmt.setString(2, schedule.getDay());
            stmt.setString(3, schedule.getTime());
            stmt.setInt(4, schedule.isCriticalAlertEnabled() ? 1 : 0);

            if (schedule.getEmergencyContactId() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setInt(5, schedule.getEmergencyContactId());
            }

            stmt.setInt(6, schedule.getMissedWindowMinutes());

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
    // EMERGENCY CONTACTS
    // -----------------------------

    public void addEmergencyContact(String name, String email) {
        String sql = """
                INSERT INTO emergency_contacts (user_id, name, email)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, name.trim());
            stmt.setString(3, email.trim());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<EmergencyContact> getEmergencyContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();

        String sql = """
                SELECT id, name, email
                FROM emergency_contacts
                WHERE user_id = ?
                ORDER BY name
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contacts.add(new EmergencyContact(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contacts;
    }

    public EmergencyContact getEmergencyContactById(Integer contactId) {
        if (contactId == null) return null;

        String sql = """
                SELECT id, name, email
                FROM emergency_contacts
                WHERE user_id = ? AND id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, contactId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new EmergencyContact(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void deleteEmergencyContact(int contactId) {
        String sql = """
                DELETE FROM emergency_contacts
                WHERE user_id = ? AND id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, contactId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------
    // CRITICAL ALERT LOGS
    // -----------------------------

    public Integer getScheduleId(MedicationSchedule schedule) {
        return scheduleIds.get(schedule);
    }

    public boolean hasCriticalAlertBeenSent(MedicationSchedule schedule, LocalDate date) {
        Integer scheduleId = getScheduleId(schedule);
        if (scheduleId == null) return true;

        String sql = """
                SELECT id
                FROM critical_alert_logs
                WHERE user_id = ? AND schedule_id = ? AND alert_date = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, scheduleId);
            stmt.setString(3, date.toString());

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void markCriticalAlertSent(MedicationSchedule schedule, LocalDate date) {
        Integer scheduleId = getScheduleId(schedule);
        if (scheduleId == null) return;

        String sql = """
                INSERT OR IGNORE INTO critical_alert_logs
                    (user_id, schedule_id, alert_date, sent_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, scheduleId);
            stmt.setString(3, date.toString());
            stmt.setString(4, java.time.LocalDateTime.now().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------- Notification Setup -----------------------------
    public record AlertCandidate(SavedMedication med, MedicationSchedule sched) {}

    public AlertCandidate getLatestMissedDoseCandidate() {
        LocalDate today = LocalDate.now();
        String todayShort = today.getDayOfWeek().toString().substring(0, 3);

        java.time.LocalTime now = java.time.LocalTime.now();

        AlertCandidate latest = null;
        int latestMinutes = -1;

        for (SavedMedication med : medications) {
            for (MedicationSchedule sched : med.getSchedules()) {

                if (!sched.getDay().equalsIgnoreCase(todayShort)) continue;
                //if (!sched.isCriticalAlertEnabled()) continue;

                Boolean status = getDoseStatus(today, sched);
                if (status != null) continue;

                if (hasCriticalAlertBeenSent(sched, today)) continue;

                int schedMinutes = parseTimeToMinutes(sched.getTime());
                int alertMinutes = schedMinutes + sched.getMissedWindowMinutes();

                int nowMinutes = now.getHour() * 60 + now.getMinute();

                if (nowMinutes < alertMinutes) continue;

                if (alertMinutes > latestMinutes) {
                    latestMinutes = alertMinutes;
                    latest = new AlertCandidate(med, sched);
                }
            }
        }

        return latest;
    }

    private int parseTimeToMinutes(String timeStr) {
        try {
            var formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a");
            var t = java.time.LocalTime.parse(timeStr.trim().toUpperCase(), formatter);
            return t.getHour() * 60 + t.getMinute();
        } catch (Exception e) {
            return -1;
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
