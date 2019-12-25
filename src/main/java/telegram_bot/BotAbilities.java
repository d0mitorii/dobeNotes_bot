package telegram_bot;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.*;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;


public class BotAbilities implements AbilityExtension {

    private final MessageSender sender;
    private final SilentSender silent;
    private final DatabaseManager dbManager;
    private final String BOT_USERNAME;

    public BotAbilities(MessageSender sender, SilentSender silent, DatabaseManager dbManager, String BOT_USERNAME) {
        this.sender = sender;
        this.silent = silent;
        this.dbManager = dbManager;
        this.BOT_USERNAME = BOT_USERNAME;
    }

    public String nameAndInfo(Ability name) {
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
                    dbManager.addUserName(ctx);
                    silent.send("Hello, " +dbManager.getUserName(ctx.chatId()) + "!\nI am a bot for notes.", ctx.chatId());
                    silent.send("Here is my list of commands:\n" +
                            nameAndInfo(addNote()) +
                            nameAndInfo(createFolder()) +
                            nameAndInfo(listNotes()) +
                            nameAndInfo(searchNotes()),
                            ctx.chatId());
                    silent.execute(Keyboards.addKeyBoard("They created me:\n@domitorii, @Bfl4t", ctx));
                })
                .build();
    }

    public Ability createFolder() {
        return Ability.builder()
                .name("newfolder")
                .info("Create new folder")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> silent.forceReply("Enter folder name", ctx.chatId()))
                .reply(upd -> {
                            silent.send(upd.getMessage().getText() + " created", upd.getMessage().getChatId());
                        },
                        MESSAGE,
                        REPLY,
                        isReplyToBot(),
                        isReplyToMessage("Enter folder name"))
                .build();
    }

    public Ability addNote() {
        String replyMessage = "Input your note";
        return Ability.builder()
                .name("addnote")
                .info("Adds a note")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> silent.forceReply(replyMessage, ctx.chatId()))
                .reply(upd -> {
                            dbManager.addNote(upd.getMessage().getChatId(), upd.getMessage().getText());
                            silent.execute(Keyboards.addKeyBoardCallBack(upd.getMessage().getText(), upd));
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
                .action(ctx ->  {
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
                            long userID =upd.getMessage().getChatId();
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



    public Reply editNote() {
        return Reply.of(upd -> {
                    silent.send("editing", upd.getCallbackQuery().getMessage().getChatId());
                },
                CALLBACK_QUERY,
                isEditCommand()
        );
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

    private Predicate<Update> isEditCommand() {
        return upd -> upd.getCallbackQuery().getData().contentEquals("edit");
    }

}
