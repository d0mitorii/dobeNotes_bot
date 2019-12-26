package telegram_bot;

import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class Keyboards {

    public static SendMessage addKeyBoard(String text, MessageContext upd) {

        InlineKeyboardMarkup InlineKeyboardMarkup = new InlineKeyboardMarkup();
        SendMessage sendMessage = new SendMessage();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
        keyboardFirstRow.add(new InlineKeyboardButton("My repositories").setUrl("https://github.com/d0mitorii/dobeNotes_bot"));
        keyboard.add(keyboardFirstRow);
        InlineKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(InlineKeyboardMarkup);
        sendMessage.setChatId(upd.chatId());
        return sendMessage;
    }

    public static SendMessage addReplyKeyBoard(String text, Object object) {
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add("Add");
        keyboardRowFirst.add("Find");
        keyboardRowFirst.add("View all");

        keyboard.add(keyboardRowFirst);

        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setText(text);

        if (object instanceof MessageContext) {
            sendMessage.setChatId(((MessageContext) object).chatId());
        } else if (object instanceof Update) {
            sendMessage.setChatId(((Update) object).getMessage().getChatId());
        }
        sendMessage.setReplyMarkup(replyKeyboardMarkup.setResizeKeyboard(true));
        return sendMessage;
    }

}
