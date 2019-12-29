package telegram_bot;

import jdk.internal.net.http.common.Pair;

import java.util.*;

public class NoteManager {
    private static final DatabaseManager dbManager = new DatabaseManager();

    public void addNote(Long userID, String note) {
        addNote(userID, note, "untitled");
    }

    public void addNote(Long userID, String note, String noteName) {
        addNote(userID, note, "Misc", noteName);
    }

    public void addNote(Long userID, String content, String folder, String noteName) {
        dbManager.insertNote(userID, content, folder, verifyNoteNameUnambiguity(userID, noteName));
    }

    public ArrayList<String> searchUserNotesByName(Long userID, String searchString) {
        Set<Pair<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);
        ArrayList<String> foundNotes = new ArrayList<>();

        for (Pair<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.second) {
                String noteName = dbManager.getNoteName(noteID);
                if (noteName.toLowerCase().contains(searchString.toLowerCase())) {
                    String output = dbManager.getFolder(noteID)
                            + "\\"
                            + dbManager.getNoteName(noteID)
                            + "\n"
                            + dbManager.getNote(noteID);
                    foundNotes.add(output);
                }
            }
        }

        return foundNotes;
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
