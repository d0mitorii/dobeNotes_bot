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

}
