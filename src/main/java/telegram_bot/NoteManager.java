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

    public String getNote(String noteName) {
        UUID noteID = dbManager.getNoteID(noteName);
        if (noteID == null) {
            return null;
        } else {
            return getNote(noteID);
        }
    }

    public String getNote(UUID noteID) {
        return getFolder(noteID)
                + "\\"
                + getNoteName(noteID)
                + "\n"
                + getNoteContent(noteID);
    }

    public String editNoteContent(String noteName, String newContent) {
        UUID noteID = dbManager.getNoteID(noteName);
        if (noteID == null) {
            return null;
        }
        dbManager.editNoteContent(noteID,newContent);
        return getNote(noteID);
    }

    public String editNoteName(String noteName, String newName, Long userID) {
        UUID noteID = dbManager.getNoteID(noteName);
        if (noteID == null) {
            return null;
        }
        dbManager.editNoteName(noteID, verifyNoteNameUnambiguity(userID, newName));
        return getNote(noteID);
    }

    public String editNoteFolder(String noteName, String newFolder, Long userID) {
        UUID noteID = dbManager.getNoteID(noteName);
        if (noteID == null) {
            return null;
        }
        dbManager.editNoteFolder(userID, noteID, newFolder);
        return getNote(noteID);
    }

    public String deleteNote(String noteName, Long userID) {
        UUID noteID = dbManager.getNoteID(noteName);
        if (noteID == null) {
            return null;
        }
        if (dbManager.deleteNote(noteID, userID)) {
            return "note deleted";
        } else {
            return null;
        }
    }


    public ArrayList<String> searchUserNotesByName(Long userID, String searchString) {
        Set<AbstractMap.SimpleEntry<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);
        ArrayList<String> foundNotes = new ArrayList<>();

        for (AbstractMap.SimpleEntry<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.getValue()) {
                String noteName = getNoteName(noteID);
                if (noteName.toLowerCase().contains(searchString.toLowerCase())) {
                    foundNotes.add(getNote(noteID));
                }
            }
        }

        return foundNotes;
    }

    public ArrayList<String> listUserNotes(Long userID) {
        Set<AbstractMap.SimpleEntry<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);
        ArrayList<String> notes = new ArrayList<>();
        if (folderSetWithNotes == null) {
            return null;
        }

        for (AbstractMap.SimpleEntry<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.getValue()) {
                    notes.add(getNote(noteID));
            }
        }

        return notes;
    }

    public ArrayList<String> listUserFolders(Long userID) {
        Set<String> folderSet = dbManager.getFolderSet(userID);
        ArrayList<String> folders = new ArrayList<>();

        for(String folder: folderSet) {
            folders.add(folder);
        }
        return folders;
    }

    public void addUserName(MessageContext msgContext) {
        dbManager.addUserName(msgContext);
    }

    public String getNoteContent(UUID noteID) {
        String note = dbManager.getNoteContent(noteID);
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
        Set<AbstractMap.SimpleEntry<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);
        if (folderSetWithNotes == null) {
            return name;
        }

        Set<String> noteNames = new HashSet<>();
        for (AbstractMap.SimpleEntry<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.getValue()) {
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
