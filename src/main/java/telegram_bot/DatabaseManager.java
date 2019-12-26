package telegram_bot;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;

import java.io.File;
import java.util.*;

public class DatabaseManager {

    private final DBContext db;

    public DatabaseManager() {
        String sep = File.separator;
        db = MapDBContext.onlineInstance("." + sep + "src" + sep + "main" + sep + "resources" + sep + "dobeDB");
    }

    public void addNote(Long userID, String note) {
        Map<Long, ArrayList<UUID>> notesIdMap = db.getMap("USERID_TO_NOTEID_ARRAY");
        Map<UUID, String> notesMap = db.getMap("NOTEID_TO_NOTE");
        ArrayList<UUID> notesId = notesIdMap.get(userID);
        if (notesId == null) {
            notesId = new ArrayList<>();
        }
        UUID noteId = UUID.randomUUID();
        notesId.add(noteId);
        notesIdMap.put(userID, notesId);
        notesMap.put(noteId, note);
    }

    public void addUserName(MessageContext msgContext) {
        String userName = msgContext.user().getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = msgContext.user().getFirstName() + " " + msgContext.user().getLastName();
        }
        Long userID = msgContext.chatId();
        Map<Long, String> userNamesMap = db.getMap("USERID_TO_USERNAME");
        userNamesMap.put(userID, userName);
    }

    public String getUserName(Long userID) {
        Map<Long, String> userNamesMap = db.getMap("USERID_TO_USERNAME");
        return userNamesMap.get(userID);
    }


    public ArrayList<String> getUserNotes(Long userID) {
        Map<Long, ArrayList<UUID>> notesIdMap = db.getMap("USERID_TO_NOTEID_ARRAY");
        Map<UUID, String> notesMap = db.getMap("NOTEID_TO_NOTE");
        ArrayList<UUID> notesId = notesIdMap.get(userID);
        if (notesId == null) {
            return null;
        }

        ArrayList<String> notes = new ArrayList<>();
        for (UUID noteId : notesId) {
            String note = notesMap.get(noteId);
            notes.add(note);
        }

        return notes;
    }

    public ArrayList<String> searchUserNotes(Long userID, String searchString) {
        ArrayList<String> userNotes = getUserNotes(userID);
        if (userNotes == null) {
            return null;
        }

        ArrayList<String> foundNotes = new ArrayList<>();
        for (String note : userNotes) {
            if (note.contains(searchString)) {
                foundNotes.add(note);
            }
        }

        return foundNotes;
    }
}

