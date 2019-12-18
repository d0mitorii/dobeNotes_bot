package telegram_bot;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class BOT extends AbilityBot {

    private static final String BOT_USERNAME = "dobeNotes_bot";
    private static final DatabaseManager dbManager = new DatabaseManager();

    public BOT(DefaultBotOptions botOptions) {
        super(System.getenv("TOKEN"), BOT_USERNAME, botOptions);
    }

    public BOT() {
        super(System.getenv("TOKEN"), BOT_USERNAME);
    }

    public AbilityExtension botAbilities() {
        return new BotAbilities(sender, silent, dbManager, BOT_USERNAME);
    }

    @Override
    public int creatorId() {
        return 0;
    }
}