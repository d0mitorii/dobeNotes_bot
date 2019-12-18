package telegram_bot;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;

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

    public void addNote(Long userID, String note) {
        Map<Long, ArrayList<UUID>> notesIdMap = db.getMap("USER_NOTEID_LISTS");
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

    public void addUserName(MessageContext msgContext) {
        String userName = msgContext.user().getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = msgContext.user().getFirstName() + " " + msgContext.user().getLastName();
        }
        Long userID = msgContext.chatId();
        Map<Long, String> userNamesMap = db.getMap("USERNAMES");
        userNamesMap.put(userID, userName);
    }

    public String getUserName(Long userID) {
        Map<Long, String> userNamesMap = db.getMap("USERNAMES");
        return userNamesMap.get(userID);
    }


    public ArrayList<String> getUserNotes(Long userID) {
        Map<Long, ArrayList<UUID>> notesIdMap = db.getMap("USER_NOTEID_LISTS");
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

