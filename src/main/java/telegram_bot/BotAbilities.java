package telegram_bot;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.*;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;


public class BotAbilities implements AbilityExtension {

    private final SilentSender silent;
    private final NoteManager noteManager;
    private final String BOT_USERNAME;

    BotAbilities(SilentSender silent, NoteManager noteManager, String BOT_USERNAME) {
        this.silent = silent;
        this.noteManager = noteManager;
        this.BOT_USERNAME = BOT_USERNAME;
    }

    private String nameAndInfo(Ability name) {
        return "/" + name.name() + " - " + name.info() + "\n";
    }


    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Displays bot info")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    noteManager.addUserName(ctx);
                    silent.send("Hello, " + noteManager.getUserName(ctx.chatId()) + "\nI am a bot for notes.", ctx.chatId());
                    silent.send("Here is my list of commands:\n" +
                            nameAndInfo(addNote()) +
                            nameAndInfo(listNotes()) +
                            nameAndInfo(listFolders()) +
                            nameAndInfo(search()) +
                            nameAndInfo(editNote()) +
                            nameAndInfo(editNoteName()) +
                            nameAndInfo(editFolderName()) +
                            nameAndInfo(changeFolder()) +
                            nameAndInfo(deleteNote()) +
                            nameAndInfo(deleteFolder()), ctx.chatId());
                    silent.execute(Keyboards.addKeyBoard("They created me:\n@domitorii, @Bfl4t", ctx));
                })
                .build();
    }

    public Ability addNote() {
        String replyMessage = "Input your note";
        List<String> arguments = new ArrayList<>();
        return Ability.builder()
                .name("add")
                .info("Adds a note.\n  Possible arguments:\n      1)no arguments;\n      2)<Note Name>;\n      3)<Folder Name>  <Note Name>")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    arguments.clear();
                    switch (ctx.arguments().length) {
                        case 1:
                            arguments.add(ctx.firstArg());
                            break;
                        case 2:
                            arguments.add(ctx.firstArg());
                            arguments.add(ctx.secondArg());
                            break;
                    }
                    silent.forceReply(replyMessage, ctx.chatId());
                })
                .reply(upd -> {
                            Long chatID = upd.getMessage().getChatId();
                            String textNote = upd.getMessage().getText();

                            switch (arguments.size()) {
                                case 0:
                                    noteManager.addNote(chatID, textNote);
                                    break;
                                case 1:
                                    noteManager.addNote(chatID, textNote, arguments.get(0));
                                    break;
                                case 2:
                                    noteManager.addNote(chatID, textNote, arguments.get(0), arguments.get(1));
                                    break;
                                default:
                                    silent.send("I don't understand", chatID);
                            }
                            String text = "Note added:\n" + textNote;
                            silent.send(text, chatID);
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessage))
                .build();
    }

    public Ability listNotes() {
        return Ability.builder()
                .name("listnotes")
                .info("View all your notes")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {

                    Long chatID = ctx.chatId();

                    if (noteManager.listUserNotes(chatID) != null) {
                        for (String note : noteManager.listUserNotes(chatID)) {
                            silent.send(note, chatID);
                        }
                    } else {
                        silent.send("You don't have any notes", chatID);
                    }
                })
                .build();
    }

    public Ability listFolders() {
        return Ability.builder()
                .name("listfolders")
                .info("View all your folders")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {

                    Long chatID = ctx.chatId();

                    if (noteManager.listUserNotes(chatID) != null) {
                        for (String folder : noteManager.listUserFolders(chatID)) {
                            silent.send(folder, chatID);
                        }
                    } else {
                        silent.send("You don't have any folders", chatID);
                    }
                })
                .build();
    }

    public Ability search() {
        String[] text = new String[2];
        return Ability.builder()
                .name("search")
                .info("Search note")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx ->
                        silent.execute(Keyboards.addReplyKeyboard(ctx)))
                .reply(upd -> {
                        text[0] = upd.getMessage().getText();
                        silent.forceReply("Enter your search term", upd.getMessage().getChatId());
                })
                .reply(upd -> {
                    text[1] = upd.getMessage().getText();
                    Long chatID = upd.getMessage().getChatId();

                    switch (text[0]) {
                        case "Content":
                            for (String note: noteManager.searchNotes(chatID, text[1], NoteManager.SearchType.CONTENT)){
                                silent.send(note, chatID);
                            }
                            break;

                        case "Note name":
                            for (String note: noteManager.searchNotes(chatID, text[1], NoteManager.SearchType.NAME)){
                                silent.send(note, chatID);
                            }
                            break;

                        case "Tag":
                            for (String note: noteManager.searchNotes(chatID, text[1], NoteManager.SearchType.TAG)){
                                silent.send(note, chatID);
                            }
                            break;

                        default:
                            silent.send("I don't understand", chatID);
                            break;
                    }
                })
                .build();
    }

//    public Ability search() {
//        List<String> arguments = new ArrayList<>();
//        return Ability.builder()
//                .name("search")
//                .info("<notename> <text>")
//                .privacy(PUBLIC)
//                .locality(ALL)
//                .input(0)
//                .action(ctx -> {
//                    Long chatID = ctx.chatId();
//                    arguments.clear();
//
//                    switch (ctx.arguments().length) {
//                        case 1:
//                            arguments.add(ctx.firstArg()); //имя заметки
//                            break;
//                        case 2:
//                            arguments.add(ctx.firstArg()); // content/notename/tag
//                            arguments.add(ctx.secondArg()); // text
//                            break;
//                    }
//
//                    switch (ctx.arguments().length) {
//                        case 1:
//                            for (String note: noteManager.searchNotes(chatID, arguments.get(0), NoteManager.SearchType.NAME)){
//                                silent.send(note, chatID);
//                            }
//                            break;
//                        case 2:
//                            switch (arguments.get(0)) {
//                                case "content":
//                                    for (String note: noteManager.searchNotes(chatID, arguments.get(1), NoteManager.SearchType.CONTENT)){
//                                        silent.send(note, chatID);
//                                    }
//                                    break;
//                                case "notename":
//                                    for (String note: noteManager.searchNotes(chatID, arguments.get(1), NoteManager.SearchType.NAME)) {
//                                        silent.send(note, chatID);
//                                    }
//                                    break;
//                                case "tag":
//                                    for (String note: noteManager.searchNotes(chatID, arguments.get(1), NoteManager.SearchType.TAG)){
//                                        silent.send(note, chatID);
//                                    }
//                                    break;
//                                default:
//                                    silent.send("I don't understand", chatID);
//                                    break;
//                            }
//                            break;
//                        default:
//                            silent.send("I don't understand", chatID);
//                            break;
//                    }
//                })
//                .build();
//    }

    public Ability editNote() {
        String replyMessageNoteName = "Input edit name";
        String replyMessageNewContent = "Input content";
        String[] noteName = new String[1];
        return Ability.builder()
                .name("editnote")
                .info("Edit note")
                .input(0)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    silent.forceReply(replyMessageNoteName, ctx.chatId());
                })
                .reply(upd -> {
                            noteName[0] = upd.getMessage().getText();
                            silent.forceReply(replyMessageNewContent, upd.getMessage().getChatId());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageNoteName))
                .reply(upd -> {
                            silent.send(noteManager.editNoteContent(upd.getMessage().getChatId(), noteName[0], upd.getMessage().getText()), upd.getMessage().getChatId());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageNewContent))
                .build();
    }

    public Ability editNoteName() {
        String replyMessageOldName = "Input old note name";
        String replyMessageNewName = "Input new note name";
        String[] nameNote = new String[1];
        return Ability.builder()
                .name("renamenote")
                .info("Edit note name")
                .input(0)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    silent.forceReply(replyMessageOldName, ctx.chatId());
                })
                .reply(upd -> {
                            nameNote[0] = upd.getMessage().getText();
                            silent.forceReply(replyMessageNewName, upd.getMessage().getChatId());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageOldName))
                .reply(upd -> {
                            Long chatID = upd.getMessage().getChatId();
                            silent.send(noteManager.editNoteName(chatID, nameNote[0], upd.getMessage().getText()), chatID);
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageNewName))
                .build();
    }

    public Ability editFolderName() {
        String replyMessageOldName = "Input old folder name";
        String replyMessageNewName = "Input new folder name";
        String[] nameFolder = new String[1];
        return Ability.builder()
                .name("renamefolder")
                .info("Edit folder name")
                .input(0)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    silent.forceReply(replyMessageOldName, ctx.chatId());
                })
                .reply(upd -> {
                            nameFolder[0] = upd.getMessage().getText();
                            silent.forceReply(replyMessageNewName, upd.getMessage().getChatId());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageOldName))
                .reply(upd -> {
                            Long chatID = upd.getMessage().getChatId();
                            silent.send(noteManager.renameFolder(chatID, nameFolder[0], upd.getMessage().getText()), chatID);
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageNewName))
                .build();
    }

    public Ability deleteNote() {
        String replyMessage = "Input note name";
        return Ability.builder()
                .name("deletenote")
                .info("Delete note")
                .input(0)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    silent.forceReply(replyMessage, ctx.chatId());
                })
                .reply(upd -> {
                            Long chatID = upd.getMessage().getChatId();
                            silent.send(noteManager.deleteNote(chatID, upd.getMessage().getText()), chatID);
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessage))
                .build();
    }

    public Ability deleteFolder() {
        String replyMessage = "Input folder name";
        return Ability.builder()
                .name("deletefolder")
                .info("Delete folder")
                .input(0)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    silent.forceReply(replyMessage, ctx.chatId());
                })
                .reply(upd -> {
                            Long chatID = upd.getMessage().getChatId();
                            silent.send(noteManager.deleteFolder(chatID, upd.getMessage().getText()), chatID);
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessage))
                .build();
    }

    public Ability changeFolder() {
        String replyMessageOldName = "Input name note";
        String replyMessageNewName = "Input name new folder";
        String[] nameNote = new String[1];
        return Ability.builder()
                .name("change")
                .info("Change note's folder")
                .input(0)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    silent.forceReply(replyMessageOldName, ctx.chatId());
                })
                .reply(upd -> {
                            nameNote[0] = upd.getMessage().getText();
                            silent.forceReply(replyMessageNewName, upd.getMessage().getChatId());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageNewName))
                .reply(upd -> {
                            Long chatID = upd.getMessage().getChatId();
                            String editedNote = noteManager.editNoteFolder(chatID, nameNote[0], upd.getMessage().getText());
                            if (editedNote == null) {
                                silent.send("no note found", chatID);
                            }
                            silent.send("note edited:\\n" + editedNote, chatID);
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageNewName))
                .build();
    }

    private Predicate<Update> isReplyToMessage(String message) {
        return upd -> {
            Message reply = upd.getMessage().getReplyToMessage();
            return reply.hasText() && reply.getText().equalsIgnoreCase(message);
        };
    }

    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(BOT_USERNAME);
    }

}
