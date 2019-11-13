package telegram_bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.*;
import java.util.Properties;

public class BOT extends TelegramLongPollingCommandBot {

    private static final String BOT_USERNAME = "dobeNotes_bot";
    private static String BOT_TOKEN;

    //        BOT(DefaultBotOptions botOptions) {
//        super(botOptions, BOT_USERNAME);
//        register(new HelloCmd());
//        register(new CoinCmd());
//        register(new KeyBoardCmd());
//        register(new KeyBoardCmdDel());
//        register(new InlineKeyBoardCmd());
//        BOT_TOKEN = getProperties().getProperty("BOT_TOKEN");
//    }
    public BOT() {
        super(BOT_USERNAME);
        BOT_TOKEN = System.getenv("TOKEN");
    }

    @Override
    public void processNonCommandUpdate(Update update) {

        Message message = update.getMessage();
        SendMessage sendMessage;

        if (message.getText().equals("Hello")) {
            sendMessage = new SendMessage(message.getChatId(), "Sam ty hello");
        } else {
            sendMessage = new SendMessage(message.getChatId(), message.getText());
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }


}