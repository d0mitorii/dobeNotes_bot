package telegram_bot;

import jdk.internal.net.http.common.Pair;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.MessageContext;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseManager {

//    private static final String USERID_TO_NOTEID_ARRAY = "USERID_TO_NOTEID_ARRAY";
//    private static final String NOTEID_TO_NOTE = "NOTEID_TO_NOTE";
//    private static final String NOTEID_TO_NOTENAME = "NOTEID_TO_NOTENAME";
//    private static final String NOTEID_TO_FOLDER = "NOTEID_TO_FOLDER";
//    private static final String USERID_TO_FOLDER_SET = "USERID_TO_FOLDER_SET";
//    private static final String USERID_TO_USERNAME = "USERID_TO_USERNAME";

    private static final String USERID_TO_USERNAME = "USERID_TO_USERNAME";
    private static final String USERID_TO_FOLDERS = "USERID_TO_FOLDERS";
    private static final String USERID_TO_NOTES = "USERID_TO_NOTES";
    private static final String FOLDER_TO_NOTES = "FOLDER_TO_NOTES";
    private static final String NOTE_TO_CONTENT = "NOTE_TO_CONTENT";
    private static final String NOTE_TO_MESSAGEID = "NOTE_TO_MESSAGEID";
    private static final String NOTE_TO_NOTENAME = "NOTE_TO_NOTENAME";
    private static final String NOTE_TO_TAGS = "NOTE_TO_TAGS";
    private static final String NOTE_TO_FOLDER = "NOTE_TO_FOLDER";
    private final DBContext db;


    public DatabaseManager() {
        String sep = File.separator;
        db = MapDBContext.onlineInstance("." + sep + "src" + sep + "main" + sep + "resources" + sep + "dobeDB");
    }


    public UUID insertNote(Long userID, String content, String folder, String noteName) {
        UUID noteID = UUID.randomUUID();
        editNoteContent(noteID, content);
        editNoteFolder(userID, noteID, folder);
        editNoteName(noteID, noteName);
        //updateNoteTags(noteID);
        db.commit();
        return noteID;
    }

    public void editNoteContent(UUID noteID, String newContent) {
            Map<UUID, String> noteContentMap = db.getMap(NOTE_TO_CONTENT);
            noteContentMap.put(noteID, newContent);
            db.commit();
    }



    public void editNoteName(UUID noteID, String newName) {
        Map<UUID, String> noteNameMap = db.getMap(NOTE_TO_NOTENAME);
        noteNameMap.put(noteID, newName);
        db.commit();
    }


    public void editNoteFolder(Long userID, UUID noteID, String newFolder) {
        addFolder(userID, newFolder);

        Map<Pair<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        Map<UUID, String> noteToFolderMap = db.getMap(NOTE_TO_FOLDER);

        String oldFolder = noteToFolderMap.get(noteID);
        if (oldFolder != null) {
            Pair<Long,String> oldFolderPair = new Pair<>(userID, oldFolder);
            Set<UUID> oldNoteSet = folderToNotesMap.get(oldFolderPair);
            if (oldNoteSet != null) {
                oldNoteSet.remove(noteID);
                folderToNotesMap.put(oldFolderPair, oldNoteSet);
            }
        }

        noteToFolderMap.put(noteID, newFolder);
        Pair<Long,String> folderPair = new Pair<>(userID, newFolder);
        Set<UUID> noteSet = folderToNotesMap.get(folderPair);
        if (noteSet == null) {
            noteSet = new HashSet<>();
        }
        noteSet.add(noteID);
        folderToNotesMap.put(folderPair, noteSet);

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

//
//    private void updateNoteTags(UUID noteID) {
//        Map<UUID, String> noteContentMap = db.getMap(NOTE_TO_CONTENT);
//        String
//    }
//
//    public List<String> getTags(String content) {
//        String[] contentParts = content.split("#");
//        List<String> tags = new ArrayList<>();
//        for(String part : contentParts) {
//            part.replace("[^0-9a-zA-Z]", "");
//        }
//        Pattern tagPattern = Pattern.compile("#(\\S+)");
//        Matcher matcher = tagPattern.matcher(content);
//        List<String> tags = new ArrayList<String>();
//        while (matcher.find()) {
//            tags.add(matcher.group(1));
//        }
//        System.out.println(tags);
//
//    }

    public String getNote(UUID noteID) {
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

    public String getUserName(Long userID) {
        Map<Long, String> userNamesMap = db.getMap(USERID_TO_USERNAME);
        return userNamesMap.get(userID);
    }



    public Set<Pair<String, Set<UUID>>> getFolderSetWithNotes(Long userID) {
        Set<Pair<String, Set<UUID>>> folderSetWithNotes = new HashSet<>();

        Map<Long, Set<String>> foldersMap = db.getMap(USERID_TO_FOLDERS);
        Set<String> folderSet = foldersMap.get(userID);
        if (folderSet == null) {
            return null;
        }

        Map<Pair<Long, String>, Set<UUID>> folderToNotesMap = db.getMap(FOLDER_TO_NOTES);
        for (String folder : folderSet) {
            Set<UUID> noteSet = folderToNotesMap.get(folder);
            if (noteSet != null) {
                Pair<String, Set<UUID>> folderWithNotes = new Pair<>(folder, noteSet);
                folderSetWithNotes.add(folderWithNotes);
            }
        }
        return folderSetWithNotes;
    }


}

