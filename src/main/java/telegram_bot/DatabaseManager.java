package telegram_bot;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseManager {

    private static final String USERID_TO_USERNAME = "USERID_TO_USERNAME";
    private static final String USERID_TO_FOLDERS = "USERID_TO_FOLDERS";
    private static final String USERID_TO_NOTES = "USERID_TO_NOTES";
    private static final String FOLDER_TO_NOTES = "FOLDER_TO_NOTES";
    private static final String NOTE_TO_CONTENT = "NOTE_TO_CONTENT";
    private static final String NOTE_TO_NOTENAME = "NOTE_TO_NOTENAME";
    private static final String NOTENAME_TO_NOTEID= "NOTENAME_TO_NOTEID";
    private static final String NOTE_TO_TAGS = "NOTE_TO_TAGS";
    private static final String NOTE_TO_FOLDER = "NOTE_TO_FOLDER";

    private final DBContext db;


    public DatabaseManager() {
        String sep = File.separator;
        db = MapDBContext.onlineInstance("." + sep + "src" + sep + "main" + sep + "resources" + sep + "dobeDB");
    }

    public UUID getNoteID(String noteName, Long userID) {
        Map<AbstractMap.SimpleEntry<String, Long>, UUID> noteIdMap = db.getMap(NOTENAME_TO_NOTEID);
        return noteIdMap.get(new AbstractMap.SimpleEntry<>(noteName, userID));
    }

    public String getNoteContent(UUID noteID) {
        Map<UUID, String> noteContentMap = db.getMap(NOTE_TO_CONTENT);
        return noteContentMap.get(noteID);
    }

    public String getFolder(UUID noteID) {
        Map<UUID, String> noteToFolderMap = db.getMap(NOTE_TO_FOLDER);
        return noteToFolderMap.get(noteID);
    }

    public String getNoteName(UUID noteID) {
        Map<UUID, String> noteNamesMap = db.getMap(NOTE_TO_NOTENAME);
        return noteNamesMap.get(noteID);
    }

    public String getUserName(Long userID) {
        Map<Long, String> userNamesMap = db.getMap(USERID_TO_USERNAME);
        return userNamesMap.get(userID);
    }

    public UUID insertNote(Long userID, String content, String folder, String noteName) {
        UUID noteID = UUID.randomUUID();
        editNoteContent(noteID, content);
        editNoteFolder(userID, noteID, folder);
        editNoteName(userID, noteID, noteName);
        updateNoteTags(noteID);

        db.commit();
        return noteID;
    }

    public void editNoteContent(UUID noteID, String newContent) {
        Map<UUID, String> noteContentMap = db.getMap(NOTE_TO_CONTENT);
        noteContentMap.put(noteID, newContent);
        updateNoteTags(noteID);

        db.commit();
    }

    public void editNoteName(Long userID, UUID noteID, String newName) {
        Map<UUID, String> noteNameMap = db.getMap(NOTE_TO_NOTENAME);
        Map<AbstractMap.SimpleEntry<String, Long>, UUID> noteIdMap = db.getMap(NOTENAME_TO_NOTEID);

        String oldName = noteNameMap.get(noteID);
        if (oldName != null) {
            AbstractMap.SimpleEntry<String, Long> oldKey= new AbstractMap.SimpleEntry<>(oldName, userID);
            noteIdMap.remove(oldKey);
        }

        AbstractMap.SimpleEntry<String, Long> newKey = new AbstractMap.SimpleEntry<>(newName, userID);
        noteNameMap.put(noteID, newName);
        noteIdMap.put(newKey, noteID);

        db.commit();
    }

    public void addFolder(Long userID, String folder) {
        Map<Long, Set<String>> foldersMap = db.getMap(USERID_TO_FOLDERS);
        Set<String> folderSet = foldersMap.get(userID);
        if (folderSet == null) {
            folderSet = new HashSet<>();
        }

        folderSet.add(folder);
        foldersMap.put(userID, folderSet);

        db.commit();
    }

    public void editNoteFolder(Long userID, UUID noteID, String newFolder) {
        addFolder(userID, newFolder);

        Map<AbstractMap.SimpleEntry<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        Map<UUID, String> noteToFolderMap = db.getMap(NOTE_TO_FOLDER);

        String oldFolder = noteToFolderMap.get(noteID);
        if (oldFolder != null) {
            AbstractMap.SimpleEntry<Long,String> oldFolderPair = new AbstractMap.SimpleEntry<>(userID, oldFolder);
            Set<UUID> oldNoteSet = folderToNotesMap.get(oldFolderPair);

            if (oldNoteSet != null) {
                oldNoteSet.remove(noteID);
                folderToNotesMap.put(oldFolderPair, oldNoteSet);
                if (oldNoteSet.isEmpty()) {
                    deleteFolder(userID, oldFolder);
                }
            }
        }

        noteToFolderMap.put(noteID, newFolder);
        AbstractMap.SimpleEntry<Long,String> folderPair = new AbstractMap.SimpleEntry<>(userID, newFolder);
        Set<UUID> noteSet = folderToNotesMap.get(folderPair);
        if (noteSet == null) {
            noteSet = new HashSet<>();
        }
        noteSet.add(noteID);
        folderToNotesMap.put(folderPair, noteSet);

        db.commit();
    }

    public void addUserName(MessageContext msgContext) {
        String userName = msgContext.user().getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = msgContext.user().getFirstName() + " " + msgContext.user().getLastName();
        }
        Long userID = msgContext.chatId();
        Map<Long, String> userNamesMap = db.getMap(USERID_TO_USERNAME);
        userNamesMap.put(userID, userName);

        db.commit();
    }


    public Set<String> getFolderSet(Long userID) {
        Map<Long, Set<String>> foldersMap = db.getMap(USERID_TO_FOLDERS);
        return foldersMap.get(userID);
    }

    public Set<AbstractMap.SimpleEntry<String, Set<UUID>>> getFolderSetWithNotes(Long userID) {
        Set<String> folderSet = getFolderSet(userID);
        if (folderSet == null) {
            return null;
        }

        Map<AbstractMap.SimpleEntry<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        Set<AbstractMap.SimpleEntry<String, Set<UUID>>> folderSetWithNotes = new HashSet<>();

        for (String folder : folderSet) {
            Set<UUID> noteSet = folderToNotesMap.get(new AbstractMap.SimpleEntry<>(userID, folder));
            if (noteSet != null) {
                AbstractMap.SimpleEntry<String, Set<UUID>> folderWithNotes = new AbstractMap.SimpleEntry<>(folder, noteSet);
                folderSetWithNotes.add(folderWithNotes);
            }
        }

        return folderSetWithNotes;
    }

    public Set<UUID> getFolderNotes(Long userID, String folder) {
        Set<String> folderSet = getFolderSet(userID);
        if (folderSet == null || !folderSet.contains(folder)) {
            return null;
        }

        Map<AbstractMap.SimpleEntry<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        AbstractMap.SimpleEntry<Long, String> folderKey = new AbstractMap.SimpleEntry<>(userID, folder);
        return folderToNotesMap.get(folderKey);
    }

    public boolean deleteNote(Long userID, String noteName) {
        UUID noteID = getNoteID(noteName, userID);
        if (noteID == null) {
            return false;
        }

        String folder = getFolder(noteID);
        Set<AbstractMap.SimpleEntry<String, Set<UUID>>> folderSetWithNotes = getFolderSetWithNotes(userID);
        AbstractMap.SimpleEntry<Long, String> folderPair = new AbstractMap.SimpleEntry<>(userID, folder);
        Map<AbstractMap.SimpleEntry<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        Set<UUID> noteSet = folderToNotesMap.get(folderPair);

        if (noteSet != null) {
            noteSet.remove(noteID);
        }

        folderToNotesMap.put(folderPair, noteSet);
        if (noteSet == null || noteSet.isEmpty()) {
            deleteFolder(userID, folder);
        }

        Map<AbstractMap.SimpleEntry<String, Long>, UUID> noteIdMap = db.getMap(NOTENAME_TO_NOTEID);
        noteIdMap.remove(new AbstractMap.SimpleEntry<>(noteName, userID));

        Map<UUID, String> noteContentMap = db.getMap(NOTE_TO_CONTENT);
        noteContentMap.remove(noteID);

        Map<UUID, String> noteToFolderMap = db.getMap(NOTE_TO_FOLDER);
        noteToFolderMap.remove(noteID);

        Map<UUID, String> noteNamesMap = db.getMap(NOTE_TO_NOTENAME);
        noteNamesMap.remove(noteID);

        db.commit();
        return true;
    }

    public boolean deleteFolder(Long userID, String folder) {
        Set<String> folderSet = getFolderSet(userID);
        if (folderSet == null || !folderSet.remove(folder)) {
            return false;
        }

        Map<Long, Set<String>> userToFoldersMap = db.getMap(USERID_TO_FOLDERS);
        userToFoldersMap.put(userID, folderSet);

        Map<AbstractMap.SimpleEntry<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        AbstractMap.SimpleEntry<Long, String> folderKey = new AbstractMap.SimpleEntry<>(userID, folder);

        Set<UUID> noteSet = folderToNotesMap.get(folderKey);
        if (noteSet != null) {
            for (UUID noteID: noteSet) {
                String noteName = getNoteName(noteID);
                deleteNote(userID, noteName);
            }
        }
        folderToNotesMap.remove(folderKey);

        db.commit();
        return true;
    }

    public String renameFolder(Long userID, String oldFolderName, String newFolderName) {
        Set<String> folderSet = getFolderSet(userID);
        if (folderSet == null || folderSet.isEmpty()) {
            return "no folders";
        }
        if (folderSet.contains(newFolderName)) {
            return "collision";
        }

        Map<Long, Set<String>> userToFoldersMap = db.getMap(USERID_TO_FOLDERS);
        folderSet.add(newFolderName);
        userToFoldersMap.put(userID, folderSet);

        Map<AbstractMap.SimpleEntry<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        AbstractMap.SimpleEntry<Long, String> oldFolderKey = new AbstractMap.SimpleEntry<>(userID, oldFolderName);
        AbstractMap.SimpleEntry<Long, String> newFolderKey = new AbstractMap.SimpleEntry<>(userID, newFolderName);
        Set<UUID> noteSet = folderToNotesMap.get(oldFolderKey);
        if (noteSet != null) {
            folderToNotesMap.put(newFolderKey, noteSet);
            for (UUID noteID: noteSet) {
                editNoteFolder(userID, noteID, newFolderName);
            }
        } else {
            folderToNotesMap.put(newFolderKey, new HashSet<>());
        }

        return "success";
    }

    public List<String> getNoteTags(UUID noteID) {
        String content = getNoteContent(noteID);
        if (content == null) {
            return null;
        }
        return getTags(content);
    }

    private List<String> updateNoteTags(UUID noteID) {
        List<String> tags = getNoteTags(noteID);
        if (tags == null) {
            return null;
        }
        Map<UUID, List<String>> noteToTagsMap = db.getMap(NOTE_TO_TAGS);
        noteToTagsMap.put(noteID, tags);
        db.commit();
        return tags;
    }

    private List<String> getTags(String content) {
        Pattern tagPattern = Pattern.compile("#([0-9a-zA-Z]+)");
        Matcher matcher = tagPattern.matcher(content);
        List<String> tags = new ArrayList<>();

        while (matcher.find()) {
            tags.add(matcher.group(1));
        }

        return tags;
    }
}

