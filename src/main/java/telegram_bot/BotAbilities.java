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
    private final DatabaseManager dbManager;
    private final String BOT_USERNAME;

    BotAbilities(SilentSender silent, DatabaseManager dbManager, String BOT_USERNAME) {
        this.silent = silent;
        this.dbManager = dbManager;
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
                    dbManager.addUserName(ctx);
                    silent.send("Hello, " + dbManager.getUserName(ctx.chatId()) + "!\nI am a bot for notes.", ctx.chatId());
                    silent.send("Here is my list of commands:\n" +
                            nameAndInfo(addNote()) +
                            nameAndInfo(searchNotes()) +
                            nameAndInfo(listNotes()), ctx.chatId());
                    silent.execute(Keyboards.addKeyBoard("They created me:\n@domitorii, @Bfl4t", ctx));
                })
                .build();
    }

    public Ability addNote() {
        String replyMessage = "Input your note";
        List<String> arguments = new ArrayList<>();
        return Ability.builder()
                .name("addnote")
                .info("Adds a note")
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
                    System.out.println(arguments);
                    silent.forceReply(replyMessage, ctx.chatId());
                })
                .reply(upd -> {
                            System.out.println(arguments.size());
                            switch (arguments.size()) {
                                case 0:
                                    dbManager.addNote(upd.getMessage().getChatId(), upd.getMessage().getText());
                                    break;
                                case 1:
                                    dbManager.addNote(upd.getMessage().getChatId(), upd.getMessage().getText(), arguments.get(0));
                                    break;
                                default:
                                    dbManager.addNote(upd.getMessage().getChatId(), upd.getMessage().getText(), arguments.get(0), arguments.get(1));
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

    public Ability listNotes() {
        return Ability.builder()
                .name("list")
                .info("Lists all of your notes")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    long userID = ctx.chatId();
                    ArrayList<String> notes = dbManager.getUserNotes(userID);
                    if (notes == null) {
                        silent.send("No notes found", userID);
                    } else {
                        silent.send("found " + notes.size() + " notes:", userID);
                        for (String note : notes) {
                            silent.send(note, userID);
                        }
                    }
                })
                .build();
    }

    public Ability searchNotes() {
        String replyMessage = "Input what you're searching for";
        return Ability.builder()
                .name("search")
                .info("Searches through your notes")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> silent.forceReply(replyMessage, ctx.chatId()))
                .reply(upd -> {
                            long userID = upd.getMessage().getChatId();
                            ArrayList<String> foundNotes = dbManager.searchUserNotes(userID, upd.getMessage().getText());
                            if (foundNotes == null) {
                                silent.send("No notes found", userID);
                            } else {
                                silent.send("found " + foundNotes.size() + " notes:", userID);
                                for (String note : foundNotes) {
                                    silent.send(note, userID);
                                }
                            }
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage(replyMessage))
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
