package telegram_bot;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
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
import org.telegram.telegrambots.meta.generics.BotOptions;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.Properties;

public class BOT extends AbilityBot {

    private static final String BOT_USERNAME = "dobeNotes_bot";
    private static final DatabaseManager dbManager = new DatabaseManager();


    //        BOT(DefaultBotOptions botOptions) {
//        super(botOptions, BOT_USERNAME);
//        register(new HelloCmd());
//        register(new CoinCmd());
//        register(new KeyBoardCmd());
//        register(new KeyBoardCmdDel());
//        register(new InlineKeyBoardCmd());
//        BOT_TOKEN = getProperties().getProperty("BOT_TOKEN");
//    }

    public BOT(DefaultBotOptions botOptions) {
        super(System.getenv("TOKEN"), BOT_USERNAME, botOptions);
    }

    @Override
    public int creatorId() {
        return 0;
    }
}