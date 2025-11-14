package dao;

import model.TimeEntry;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimeEntryDao {

    public boolean save(TimeEntry entry) throws SQLException {
        String sql = "INSERT INTO time_entries (description, start_time, end_time, duration, category, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entry.getDescription());
            stmt.setObject(2, entry.getStartTime());
            stmt.setObject(3, entry.getEndTime());
            stmt.setInt(4, entry.getDuration());
            stmt.setString(5, entry.getCategory());
            stmt.setInt(6, entry.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    // Update entry

    public boolean update(TimeEntry entry) throws SQLException {
        String sql = "UPDATE time_entries SET description = ?, category = ?, start_time = ?, end_time = ?, duration = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entry.getDescription());
            stmt.setString(2, entry.getCategory());
            stmt.setObject(3, entry.getStartTime());
            stmt.setObject(4, entry.getEndTime());
            stmt.setInt(5, entry.getDuration());
            stmt.setInt(6, entry.getId());
            return stmt.executeUpdate() > 0;
        }
    }


    // Delete entry
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM time_entries WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<TimeEntry> getEntriesByUser(int userId) throws SQLException {
        List<TimeEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM time_entries WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TimeEntry entry = new TimeEntry();
                entry.setId(rs.getInt("id"));
                entry.setDescription(rs.getString("description"));
                entry.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                entry.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                entry.setDuration(rs.getInt("duration"));
                entry.setCategory(rs.getString("category"));
                entry.setUserId(rs.getInt("user_id"));
                list.add(entry);
            }
        }
        return list;
    }
}
