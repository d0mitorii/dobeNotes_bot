package telegram_bot;

import org.telegram.abilitybots.api.objects.MessageContext;

import java.util.*;

public class NoteManager{
    private static final DatabaseManager dbManager = new DatabaseManager();

    public enum SearchType {
        TAG,
        CONTENT,
        NAME
    }

    public String addNote(Long userID, String content) {
        return addNote(userID, content, "untitled");
    }

    public String addNote(Long userID, String content, String noteName) {
        return addNote(userID, content, "Misc", noteName);
    }

    public String addNote(Long userID, String content, String folder, String noteName) {
        UUID noteID = dbManager.insertNote(userID, content, folder, verifyNoteNameUnambiguity(userID, noteName));
        return "note added:\n" + getNote(noteID);
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

    public List<String> listFolderNotes(Long userID, String folder) {
        Set<UUID> noteSet = dbManager.getFolderNotes(userID, folder);
        if (noteSet == null || noteSet.isEmpty()) {
            return new ArrayList<>(Arrays.asList("this folder doesn't exist"));
        }

        List<String> notes = new ArrayList<>();
        for (UUID noteID : noteSet) {
            notes.add(getNote(noteID));
        }
        return notes;
    }

    public List<String> getNoteTags(UUID noteID) {
        return dbManager.getNoteTags(noteID);
    }

    public String editNoteContent(Long userID, String noteName, String newContent) {
        UUID noteID = dbManager.getNoteID(noteName, userID);

        if (noteID == null) {
            return "a note with this name is not found";
        }

        dbManager.editNoteContent(noteID, newContent);
        return "note edited:\n" + getNote(noteID);
    }

    public String editNoteName(Long userID, String noteName, String newName) {
        UUID noteID = dbManager.getNoteID(noteName, userID);

        if (noteID == null) {
            return "a note with this name is not found";
        }

        dbManager.editNoteName(userID, noteID, verifyNoteNameUnambiguity(userID, newName));
        return "note edited:\n" + getNote(noteID);
    }

    public String editNoteFolder(Long userID, String noteName, String newFolder) {
        UUID noteID = dbManager.getNoteID(noteName, userID);

        if (noteID == null) {
            return "a note with this name is not found";
        }

        dbManager.editNoteFolder(userID, noteID, newFolder);
        return "note edited:\n" + getNote(noteID);
    }

    public String renameFolder(Long userID, String oldFolderName, String newFolderName) {
        switch(dbManager.renameFolder(userID, oldFolderName, newFolderName)) {
            case "no folders":
                return "you don't have any folders";
            case "collision":
                return "a folder with this name already exists";
            case "success":
                return oldFolderName + "->" + newFolderName + ": successfully renamed";
            case "deletion error":
                return "error deleting folder: " + oldFolderName;
            default:
                return "unexpected error";
        }
    }

    public String deleteNote(Long userID, String noteName) {
        String note = getNote(userID, noteName);
        if (note == null) {
            return "a note with this name is not found";
        }

        if (dbManager.deleteNote(userID, noteName)) {
            return "note deleted: \n" + note;
        } else {
            return "unexpected error";
        }
    }

    public String deleteFolder(Long userID, String folder) {
        if (dbManager.deleteFolder(userID, folder)) {
            return "folder " + folder + " deleted";
        } else {
            return "a folder with this name is not found";
        }
    }


    public ArrayList<String> searchNotes(Long userID, String searchString, SearchType searchType) {
        Set<AbstractMap.SimpleEntry<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);
        ArrayList<String> foundNotes = new ArrayList<>();

        for (AbstractMap.SimpleEntry<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.getValue()) {
                switch(searchType) {
                    case NAME:
                        String noteName = getNoteName(noteID);
                        if (noteName.toLowerCase().contains(searchString.toLowerCase())) {
                            foundNotes.add(getNote(noteID));
                        }
                        break;
                    case CONTENT:
                        String content = getNoteContent(noteID);
                        if (content.toLowerCase().contains(searchString.toLowerCase())) {
                            foundNotes.add(getNote(noteID));
                        }
                        break;
                    case TAG:
                        List<String> tags = getNoteTags(noteID);
                        if (tags.contains(searchString)) {
                            foundNotes.add(getNote(noteID));
                        }
                        break;
                }
            }
        }

        if (foundNotes.isEmpty()) {
            foundNotes.add("nothing found");
        }
        return foundNotes;
    }


    public ArrayList<String> listUserNotes(Long userID) {
        Set<AbstractMap.SimpleEntry<String, Set<UUID>>> folderSetWithNotes = dbManager.getFolderSetWithNotes(userID);
        ArrayList<String> notes = new ArrayList<>();

        if (folderSetWithNotes == null || folderSetWithNotes.isEmpty()) {
            notes.add("you don't have any notes");
            return notes;
        }

        for (AbstractMap.SimpleEntry<String, Set<UUID>> folderPair : folderSetWithNotes) {
            for (UUID noteID : folderPair.getValue()) {
                    notes.add(getNote(noteID));
            }
        }

        if (notes.isEmpty()) {
            notes.add("you don't have any notes");
        }
        return notes;
    }

    public ArrayList<String> listUserFolders(Long userID) {
        Set<String> folderSet = dbManager.getFolderSet(userID);
        if (folderSet == null) {
            folderSet = new HashSet<>();
        }
        if (folderSet.isEmpty()) {
            folderSet.add("you don't have any folders");
        }
        return new ArrayList<>(folderSet);
    }

    public void addUserName(MessageContext msgContext) {
        dbManager.addUserName(msgContext);
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
