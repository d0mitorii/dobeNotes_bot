package telegram_bot;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
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
import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.abilitybots.api.objects.Flag.REPLY;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;


public class BotAbilities implements AbilityExtension {

    private final MessageSender sender;
    private final SilentSender silent;
    private final DatabaseManager dbManager;
    private final String BOT_USERNAME;
    private final InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

    public BotAbilities(MessageSender sender, SilentSender silent, DatabaseManager dbManager, String BOT_USERNAME) {
        this.sender = sender;
        this.silent = silent;
        this.dbManager = dbManager;
        this.BOT_USERNAME = BOT_USERNAME;
    }

    public String nameAndInfo(Ability name) {
        return "/" + name.name() + " - " + name.info() + "\n";
    }

    public String getUSER_NAME(MessageContext upd) {
        String USER_NAME = upd.user().getUserName();
        if (USER_NAME == null || USER_NAME.isEmpty()) {
            USER_NAME = upd.user().getFirstName() + " " + upd.user().getLastName();
        }
        return USER_NAME;
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Startup")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    silent.send("Hello, " + getUSER_NAME(ctx), ctx.chatId());
                    silent.send("I am a bot for notes", ctx.chatId());
                    silent.send("Here is my list of commands:\n" +
                            nameAndInfo(addNote()), ctx.chatId());
//                    silent.send("They created me:\n@domitorii\n@Bfl4t", ctx.chatId());
                    silent.execute(addKeyBoard("They created me:\n@domitorii, @Bfl4t", ctx));
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

    private InlineKeyboardMarkup InlineKeyboardMarkup = new InlineKeyboardMarkup();

    private SendMessage addKeyBoard(String text, MessageContext upd) {
        SendMessage sendMessage = new SendMessage();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();

        keyboard.clear();
        keyboardFirstRow.clear();

        keyboardFirstRow.add(new InlineKeyboardButton("My repositories").setUrl("https://github.com/d0mitorii/dobeNotes_bot"));
        keyboard.add(keyboardFirstRow);
        InlineKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setText(text);
        sendMessage.setReplyMarkup(InlineKeyboardMarkup);
        sendMessage.setChatId(upd.chatId());
        return sendMessage;
    }

}
