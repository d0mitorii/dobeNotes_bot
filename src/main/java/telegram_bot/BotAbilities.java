package telegram_bot;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

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
                    String text;
//                    noteManager.addUserName(ctx);
//                    silent.send("Hello, " + dbManager.getUserName(ctx.chatId()) + "!\nI am a bot for notes.", ctx.chatId());
                    silent.send("Here is my list of commands:\n" +
//                            nameAndInfo(note()) +
//                            nameAndInfo(search()) +
                            nameAndInfo(listNotes()), ctx.chatId());
                    silent.execute(Keyboards.addKeyBoard("They created me:\n@domitorii, @Bfl4t", ctx));
                })
                .build();
    }

    public Ability addNote() {
        String replyMessage = "Input your note";
        List<String> arguments = new ArrayList<>();
        return Ability.builder()
                .name("note")
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
                    if (ctx.firstArg().equals("folders")) {
                        //показать папки

                    } else if (ctx.firstArg().equals("notes")) {
                        for (String note: noteManager.listUserNotes(ctx.chatId())) {
                            silent.send(note, ctx.chatId());
                        }
                    } else {
                        //ошиб очка
                    }
                })
                .build();
    }

    public Ability search() {
        String replyMessage = "Input what you're searching for";
        List<String> arguments = new ArrayList<>();
        return Ability.builder()
                .name("search")
                .info("<content/notename/tag> <text>")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    Long chatID = ctx.chatId();
                    arguments.clear();
                    switch (ctx.arguments().length) {
                        case 1:
                            for (String note: noteManager.searchUserNotesByName(chatID, arguments.get(0))) {
                                silent.send(note, chatID);
                            }
                            break;
                        case 2:
                            switch (arguments.get(0)) {
                                case "content":
                                    //поиск по контенту
                                    break;
                                case "notename":
                                    //поиск по имени заметки
                                    break;
                                case "tag":
                                    //поиск по тэгу
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            //ошибка или чо-то еще
                            break;
                    }
                    silent.forceReply(replyMessage, ctx.chatId());
                })
                .reply(upd -> {

                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessage))
                .build();
    }


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
                    silent.forceReply(replyMessageNoteName,ctx.chatId());

                })
                .reply(upd->{
                    noteName[0] = upd.getMessage().getText();
                    silent.forceReply(replyMessageNewContent, upd.getMessage().getChatId());
                },
                    MESSAGE,
                    REPLY,
                    isReplyToBot(),
                    isReplyToMessage(replyMessageNoteName))
                .reply(upd ->{
                    silent.send(noteManager.editNoteContent(noteName[0], upd.getMessage().getText()), upd.getMessage().getChatId());
                },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessageNewContent))
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
