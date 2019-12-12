package telegram_bot;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private final DBContext db;

    public DatabaseManager() {
        String sep = File.separator;
        db = MapDBContext.onlineInstance("." + sep + "src" + sep + "main" + sep + "resources" + sep + "dobeDB");
    }

    public void addNote(Integer userID, String note) {
        Map<Integer, ArrayList<UUID>> notesIdMap = db.getMap("USER_NOTEID_LISTS");
        Map<UUID, String> notesMap = db.getMap("NOTES");
        ArrayList<UUID> notesId = notesIdMap.get(userID);
        if (notesId == null) {
            notesId = new ArrayList<>();
        }
        UUID noteId = UUID.randomUUID();
        notesId.add(noteId);
        notesIdMap.put(userID, notesId);
        notesMap.put(noteId, note);
    }

    public ArrayList<String> getUserNotes(Integer userID) {
        Map<Integer, ArrayList<UUID>> notesIdMap = db.getMap("USER_NOTEID_LISTS");
        Map<UUID, String> notesMap = db.getMap("NOTES");
        ArrayList<UUID> notesId = notesIdMap.get(userID);
        if (notesId == null) {
            return new ArrayList<String>(Arrays.asList("empty"));
        }
        ArrayList<String> notes = new ArrayList<>();
        for (UUID noteId : notesId) {
            String note = notesMap.get(noteId);
            notes.add(note);
        }
        return notes;
    }

}

