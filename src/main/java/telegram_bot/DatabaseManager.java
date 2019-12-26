package telegram_bot;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;

import java.io.File;
import java.util.*;

public class DatabaseManager {

    private final DBContext db;
    private int noteNumber = 1;

    public DatabaseManager() {
        String sep = File.separator;
        db = MapDBContext.onlineInstance("." + sep + "src" + sep + "main" + sep + "resources" + sep + "dobeDB");
    }

    public void addNote(Long userID, String note) {
        addNote(userID, note, String.valueOf(noteNumber), "Misc.");
        noteNumber++;
    }

    public void addNote(Long userID, String note, String noteName) {
        addNote(userID, note, noteName, "Misc.");
    }

    public void addNote(Long userID, String note, String folder, String noteName) {
        Map<Long, ArrayList<UUID>> notesIdMap = db.getMap("USERID_TO_NOTEID_ARRAY");
        Map<UUID, String> notesMap = db.getMap("NOTEID_TO_NOTE");
        Map<UUID, String> noteNamesMap = db.getMap("NOTEID_TO_NOTENAME");
        ArrayList<UUID> notesId = notesIdMap.get(userID);
        if (notesId == null) {
            notesId = new ArrayList<>();
        }
        UUID noteID = UUID.randomUUID();
        notesId.add(noteID);
        notesIdMap.put(userID, notesId);
        notesMap.put(noteID, note);
        noteNamesMap.put(noteID, noteName);
        addFolder(userID, folder);
        Map<UUID, String> noteToFolderMap = db.getMap("NOTEID_TO_FOLDER");
        noteToFolderMap.put(noteID, folder);
        db.commit();
    }

    public void addFolder(Long userID, String folder) {
        Map<Long, HashSet<String>> folderMap = db.getMap("USERID_TO_FOLDER_SET");
        HashSet<String> folders = folderMap.get(userID);
        if (folders == null) {
            folders = new HashSet<>();
        }
        folders.add(folder);
        db.commit();
    }

    public String getFolder(UUID noteID) {
        Map<UUID, String> noteToFolderMap = db.getMap("NOTEID_TO_FOLDER");
        String folder = noteToFolderMap.get(noteID);
        if (folder == null) {
            return "noFolder";
        }
        return folder;
    }

    public String getNoteName(UUID noteID) {
        Map<UUID, String> noteNamesMap = db.getMap("NOTEID_TO_NOTENAME");
        String name = noteNamesMap.get(noteID);
        if (name == null) {
            return "noName";
        }
        return name;
    }

    public void addUserName(MessageContext msgContext) {
        String userName = msgContext.user().getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = msgContext.user().getFirstName() + " " + msgContext.user().getLastName();
        }
        Long userID = msgContext.chatId();
        Map<Long, String> userNamesMap = db.getMap("USERID_TO_USERNAME");
        userNamesMap.put(userID, userName);
        db.commit();
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
            String folder = getFolder(noteId);
            String name = getNoteName(noteId);
            notes.add(folder + "/" + name + ":\n" + note);
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
            if (note.toLowerCase().contains(searchString)) {
                foundNotes.add(note);
            }
        }

        return foundNotes;
    }
}

