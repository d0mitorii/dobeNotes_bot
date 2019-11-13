package telegram_bot;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.nio.file.Files;

public class Main {

    public static void main(String[] arg) {
        String sep = File.separator;
        DBContext db = MapDBContext.onlineInstance("." + sep + "src" + sep + "main" + sep + "resources" + sep + "dobeDB");
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();


        try {
//            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
//            botOptions.setProxyHost("51.158.102.15");
//            botOptions.setProxyPort(3128);
//            botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
//            telegramBotsApi.registerBot(new BOT(db, botOptions));
            telegramBotsApi.registerBot(new BOT(db));
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
