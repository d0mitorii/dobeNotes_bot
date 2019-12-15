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
        String sep = File.separator;
        DBContext db = MapDBContext.onlineInstance("." + sep + "src" + sep + "main" + sep + "resources" + sep + "dobeDB");
        ApiContextInitializer.init();
        //TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        Integer Id1 = 1;
        ArrayList<String> notes1 = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            String note =  "user1note" + i;
            notes1.add(note);
        }


        Integer Id2 = 2;
        ArrayList<String> notes2 = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            String note =  "user2note" + i;
            notes2.add(note);
        }


        Integer Id3 = 3;
        ArrayList<String> notes3 = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            String note =  "user3note" + i;
            notes3.add(note);
        }

        Map<Integer, ArrayList<String>> notesMap = db.getMap("Notes");
        notesMap.put(Id1, notes1);
        notesMap.put(Id2, notes2);
        notesMap.put(Id3, notes3);

        for(Integer userId : notesMap.keySet()) {
            ArrayList<String> Notes =  notesMap.get(userId);
            System.out.println(Notes.toString());
        }

        System.out.println("hello");



//        try {
////            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
////            botOptions.setProxyHost("51.158.102.15");
////            botOptions.setProxyPort(3128);
////            botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
////            telegramBotsApi.registerBot(new BOT(db, botOptions));
//
//            telegramBotsApi.registerBot(new BOT(db));
//
//        } catch (TelegramApiRequestException e) {
//            e.printStackTrace();
//        }
    }
}
