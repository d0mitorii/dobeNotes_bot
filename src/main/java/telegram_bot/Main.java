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
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class Main {

    public static void main(String[] arg) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

//        DatabaseManager dbManager = new DatabaseManager();
//
//        Integer Id1 = 1;
//        for(int i = 0; i < 10; i++) {
//            String note =  "user1note" + i;
//            dbManager.addNote(Id1, note);
//        }
//
//        Integer Id2 = 2;
//        for(int i = 0; i < 10; i++) {
//            String note =  "user2note" + i;
//            dbManager.addNote(Id2, note);
//        }
//
//        Integer Id3 = 3;
//        for(int i = 0; i < 10; i++) {
//            String note =  "user3note" + i;
//            dbManager.addNote(Id3, note);
//        }
//
//        for (int i = 1; i < 6; i++) {
//            ArrayList<String> notes = dbManager.getUserNotes(i);
//            System.out.println(notes.toString());
//        }
        System.out.println(System.getenv("TOKEN"));
        try {
//            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
//            botOptions.setProxyHost("51.158.102.15");
//            botOptions.setProxyPort(3128);
//            botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
//            telegramBotsApi.registerBot(new BOT(botOptions));
            telegramBotsApi.registerBot(new BOT());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}
