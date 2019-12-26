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

//    public static SendMessage addKeyBoardCallBack(Update upd) {
//        SendMessage sendMessage = new SendMessage();
//        InlineKeyboardMarkup InlineKeyboardMarkup = new InlineKeyboardMarkup();
//        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
//        List<InlineKeyboardButton> keyboardFirstRow = new ArrayList<>();
//
//        keyboardFirstRow.add(new InlineKeyboardButton("Edit").setCallbackData("edit").setSwitchInlineQueryCurrentChat("Jojo"));
//        keyboardFirstRow.add(new InlineKeyboardButton("Delete").setCallbackData("delete"));
//        keyboardFirstRow.add(new InlineKeyboardButton("Change folder").setCallbackData("changeFolder"));
//        keyboard.add(keyboardFirstRow);
//        InlineKeyboardMarkup.setKeyboard(keyboard);
//
//        sendMessage.setText("Note added:\n" + upd.getMessage().getText());
//        sendMessage.setReplyMarkup(InlineKeyboardMarkup);
//        sendMessage.setChatId(upd.getMessage().getChatId());
//        return sendMessage;
//    }

    public static SendMessage addReplyKeyBoard(MessageContext upd) {
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRowFirst = new KeyboardRow();
        keyboardRowFirst.add("Add");
        keyboardRowFirst.add("Find");
        keyboardRowFirst.add("View all");

        keyboard.add(keyboardRowFirst);

        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setText("Ready");
        sendMessage.setChatId(upd.chatId());

        sendMessage.setReplyMarkup(replyKeyboardMarkup.setResizeKeyboard(true));
        return sendMessage;
    }

//    public static SendMessage addReplyKeyBoard1(Update upd) {
//        SendMessage sendMessage = new SendMessage();
//        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
//        List<KeyboardRow> keyboard = new ArrayList<>();
//        KeyboardRow keyboardRowFirst = new KeyboardRow();
//        KeyboardRow keyboardRowSecond = new KeyboardRow();
//        keyboardRowFirst.add("Adds a note");
//        keyboardRowSecond.add("Edit folder");
//        keyboard.add(keyboardRowFirst);
//        keyboard.add(keyboardRowSecond);
//        replyKeyboardMarkup.setKeyboard(keyboard);
//        sendMessage.setText("Editing");
//        sendMessage.setChatId(upd.getCallbackQuery().getMessage().getChatId());
//        replyKeyboardMarkup.setResizeKeyboard(true);
//        sendMessage.setReplyMarkup(replyKeyboardMarkup);
//        return sendMessage;
//    }



}
