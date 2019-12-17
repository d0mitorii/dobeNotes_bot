package telegram_bot;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.function.Predicate;
import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.abilitybots.api.objects.Flag.REPLY;
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
                .info("Startup")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    String USER_NAME = ctx.user().getUserName();
                    if (USER_NAME == null || USER_NAME.isEmpty()) {
                        USER_NAME = ctx.user().getFirstName() + " " + ctx.user().getLastName();
                    }
                    silent.send("Hello, " + USER_NAME, ctx.chatId());
                    silent.send("I am a bot for notes", ctx.chatId());
                    silent.send("Here is my list of commands:\n" +
                            nameAndInfo(addNote()), ctx.chatId());
                })
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
                            Long userID = upd.getMessage().getChatId();
                            Message msg = upd.getMessage();
                            dbManager.addNote(userID, msg.getText());
                            silent.send("Note added:\n" + msg.getText(), upd.getMessage().getChatId());
                            System.out.println(dbManager.getUserNotes(userID));
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
