package service;

import dao.TimeEntryDao;
import model.TimeEntry;

import java.sql.SQLException;
import java.util.List;

public class TimeEntryService {

    private final TimeEntryDao dao = new TimeEntryDao();

    // Existing methods
    public List<TimeEntry> getEntriesByUser(int userId) throws SQLException {
        return dao.getEntriesByUser(userId);
    }

    public boolean addEntry(TimeEntry entry) throws SQLException {
        return dao.save(entry);
    }

    // New methods for GUI
    public boolean updateEntry(TimeEntry entry) throws SQLException {
        return dao.update(entry); // <-- we will implement this in DAO
    }

    public boolean deleteEntry(int id) throws SQLException {
        return dao.delete(id); // <-- we will implement this in DAO
    }
}
