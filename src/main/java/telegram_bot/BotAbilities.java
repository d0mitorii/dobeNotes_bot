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
                            nameAndInfo(note()) +
                            nameAndInfo(search()) +
                            nameAndInfo(list()), ctx.chatId());
                    silent.execute(Keyboards.addKeyBoard("They created me:\n@domitorii, @Bfl4t", ctx));
                })
                .build();
    }

    public Ability note() {
        String replyMessage = "Input your note";
        List<String> arguments = new ArrayList<>();
        return Ability.builder()
                .name("note")
                .info("<add/edit/delete> <Name Folder> <Name Note>")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    arguments.clear();
                    switch (ctx.arguments().length) {
                        case 1:
                            arguments.add(ctx.firstArg()); //   add/edit/delete
                            break;
                        case 2:
                            arguments.add(ctx.firstArg()); //   add/edit/delete
                            arguments.add(ctx.secondArg()); //   name note
                            break;
                        case 3:
                            arguments.add(ctx.firstArg()); //   add/edit/delete
                            arguments.add(ctx.secondArg()); //   name folder
                            arguments.add(ctx.thirdArg()); //   name note
                    }
                    silent.forceReply(replyMessage, ctx.chatId());
                })
                .reply(upd -> {
                    Long chatID = upd.getMessage().getChatId();
                    String textNote = upd.getMessage().getText();
                    switch (arguments.size()) {
                        case 1:
                            switch (arguments.get(0)) {
                                case "add":
                                    noteManager.addNote(chatID, textNote);
                                    silent.send("adding", chatID);
                                    break;
                                default:
                                    silent.send("I don't understand", chatID);
                                    break;
                            }
                            break;
                        case 2:
                            switch (arguments.get(0)) {
                                case "add":
                                    //Добавление заметки с названием заметки
                                    noteManager.addNote(chatID, textNote, arguments.get(1));
                                    silent.send("adding", chatID);
                                    break;
                                case "edit":
                                    //Редактирование заметки с названием заметки
                                    break;
                                case "delete":
                                    //Удаление заметки с названием заметки
                                    break;
                                default:
                                    //ошибка или чо-то другое
                                    break;
                            }
                            break;
                        case 3:
                            switch (arguments.get(0)) {
                                case "add":
                                    //Добавление заметки с названием заметки и папки
                                    silent.send("adding", chatID);
                                    noteManager.addNote(chatID, textNote, arguments.get(1), arguments.get(2));
                                    break;
                                case "edit":
                                    //Добавление заметки с названием заметки и папки
                                    break;
                                case "delete":
                                    //Удаление заметки с названием заметки и папки
                                    break;
                                default:
                                    //ошибка или чо-то другое
                                    break;
                            }
                            break;
                        default:
                            //ошибка аргументов или чо-то другое
                            break;
                    }
                            String text = "Note added:\n" + upd.getMessage().getText();
                            silent.send(text, upd.getMessage().getChatId());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessage))
                .build();
    }

    public Ability list() {
        return Ability.builder()
                .name("list")
                .info("<folders/notes>")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    if (ctx.firstArg().equals("folders")) {
                        //показать папки

                    } else if (ctx.firstArg().equals("notes")) {
                        System.out.println("yes");
                        for (String note: noteManager.listUserNotes(ctx.chatId())) {
                            System.out.println(note);
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
        String replyMessage = "Input edit name";
        String replyMessage1 = "Input content";
        String[] noteName = new String[1];
        return Ability.builder()
                .name("editnote")
                .info("Edit note")
                .input(1)
                .privacy(PUBLIC)
                .locality(ALL)
                .action(ctx -> {
                    silent.forceReply(replyMessage,ctx.chatId());
                })
                .reply(upd->{
                    noteName[0] = upd.getMessage().getText();
                },
                    MESSAGE,
                    REPLY,
                    isReplyToBot(),
                    isReplyToMessage(replyMessage))
                .action(ctx -> {
                    silent.forceReply(replyMessage1, ctx.chatId());
                })
                .reply(upd ->{
                    silent.send(noteManager.editNoteContent(noteName[0], upd.getMessage().getText()), upd.getMessage().getChatId());
                },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessage1))
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
