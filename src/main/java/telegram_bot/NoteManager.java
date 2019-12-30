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

    public String getNote(Long userID, String noteName) {
        UUID noteID = dbManager.getNoteID(noteName, userID);
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

    public String editNoteContent(Long userID, String noteName, String newContent) {
        UUID noteID = dbManager.getNoteID(noteName, userID);
        if (noteID == null) {
            return null;
        }
        dbManager.editNoteContent(noteID, newContent);
        return getNote(noteID);
    }

    public String editNoteName(Long userID, String noteName, String newName) {
        UUID noteID = dbManager.getNoteID(noteName, userID);
        if (noteID == null) {
            return null;
        }
        dbManager.editNoteName(userID, noteID, verifyNoteNameUnambiguity(userID, newName));
        return getNote(noteID);
    }

    public String editNoteFolder(Long userID, String noteName, String newFolder) {
        UUID noteID = dbManager.getNoteID(noteName, userID);
        if (noteID == null) {
            return null;
        }
        dbManager.editNoteFolder(userID, noteID, newFolder);
        return getNote(noteID);
    }

    public String deleteNote(Long userID, String noteName) {
        if (dbManager.deleteNote(noteName, userID)) {
            return "note deleted";
        } else {
            return "note not found";
        }
    }

    public String deleteFolder(Long userID, String folder) {
        if (dbManager.deleteFolder(userID, folder)) {
            return "folder deleted";
        } else {
            return "folder not found";
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
        if (notes.isEmpty()) {
            return null;
        }
        return notes;
    }

    public ArrayList<String> listUserFolders(Long userID) {
        Set<String> folderSet = dbManager.getFolderSet(userID);
        ArrayList<String> folders = new ArrayList<>();
        folders.addAll(folderSet);
        return folders;
    }

    public void addUserName(MessageContext msgContext) {
        dbManager.addUserName(msgContext);
    }

    public String getUserName(Long userID) {
        return dbManager.getUserName(userID);
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
