package telegram_bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {

    public static void main(String[] arg) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

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
