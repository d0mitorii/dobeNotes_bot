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
    public Ability sayHelloWorld() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello world 123", ctx.chatId()))
                .build();
    }

    public Ability playWithMe() {
        String playMessage = "Play with me!";
        return Ability.builder()
                .name("play123")
                .info("Do you want to play with me?")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> silent.forceReply(playMessage, ctx.chatId()))
// The signature of a reply is -> (Consumer<Update> action, Predicate<Update>... conditions)
// So, we first declare the action that takes an update (NOT A MESSAGECONTEXT) like the action above
// The reason of that is that a reply can be so versatile depending on the message, context becomes an inefficient wrapping
                .reply(upd -> {
// Sends message
                            silent.send("It's been nice playing with you!", upd.getMessage().getChatId());
                        },
// Now we start declaring conditions, MESSAGE is a member of the enum Flag class
// That class contains out-of-the-box predicates for your replies!
// MESSAGE means that execute the reply if it has a message
                        MESSAGE,
// REPLY means that the update must be a reply
                        REPLY,
// The reply must be to the bot
                        isReplyToBot(),
// The reply is to the playMessage
                        isReplyToMessage(playMessage)
                )
// You can add more replies by calling .reply(...)
                .build();

/*
The checks are made so that, once you execute your logic there is not need to check for the validity of the reply.
They were all made once the action logic is being executed.
*/
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
