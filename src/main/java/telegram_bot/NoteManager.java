package telegram_bot;

import jdk.internal.net.http.common.Pair;
import org.telegram.abilitybots.api.objects.MessageContext;

import java.util.*;

public class NoteManager{
    private static final DatabaseManager dbManager = new DatabaseManager();

    public UUID addNote(Long userID, String content) {
        return addNote(userID, content, "untitled");
    }

    public UUID addNote(Long userID, String content, String noteName) {
        return addNote(userID, content, "Misc", noteName);
    }

    public UUID addNote(Long userID, String content, String folder, String noteName) {
        return dbManager.insertNote(userID, content, folder, verifyNoteNameUnambiguity(userID, noteName));
    }

    public ArrayList<String> searchUserNotesByName(Long userID, String searchString) {
        Set<Pair<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);
        ArrayList<String> foundNotes = new ArrayList<>();

        for (Pair<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.second) {
                String noteName = getNoteName(noteID);
                if (noteName.toLowerCase().contains(searchString.toLowerCase())) {
                    String output = getFolder(noteID)
                            + "\\"
                            + getNoteName(noteID)
                            + "\n"
                            + getNote(noteID);
                    foundNotes.add(output);
                }
            }
        }

        return foundNotes;
    }

    public void addUserName(MessageContext msgContext) {
    }

    public String getNote(UUID noteID) {
        String note = dbManager.getNote(noteID);
        if (note == null) {
            return "error: no content";
        }
        return note;
    }

    public String getFolder(UUID noteID) {
        String folder = dbManager.getFolder(noteID);
        if (folder == null) {
            return "error: no folder";
        }
        return folder;
    }

    public String getNoteName(UUID noteID) {
        String noteName = dbManager.getNoteName(noteID);
        if (noteName == null) {
            return "error: no note name";
        }
        return noteName;
    }


    private String verifyNoteNameUnambiguity(Long userID, String name) {
        Set<Pair<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);

        Set<String> noteNames = new HashSet<>();
        for (Pair<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.second) {
                String noteName = dbManager.getNoteName(noteID);
                if (noteName != null) {

                    noteNames.add(noteName);
                }
            }
        }

        String newName = name;
        int i = 1;
        while (noteNames.contains(newName)) {
            newName = name + "(" + i + ")";
            i++;
        }

        return newName;
    }

}
