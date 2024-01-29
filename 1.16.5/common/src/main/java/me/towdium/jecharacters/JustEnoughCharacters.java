package me.towdium.jecharacters;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.towdium.jecharacters.utils.Greetings;
import me.towdium.jecharacters.utils.Match;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class JustEnoughCharacters {

    public static final String MODID = "jecharacters";
    public static final Logger logger = LogManager.getLogger("JustEnoughCharacters");

    public static void init() {
        Greetings.send(logger, MODID, PlatformUtils::isModLoaded);
        ModConfig.register();
        ModConfig.reload();
        Match.onConfigChange();
    }

    public static <S> LiteralArgumentBuilder<S> registerCommand(CommandDispatcher<S> dispatcher, Function<String, LiteralArgumentBuilder<S>> literal) {
        return JechCommand.register(literal, dispatcher, PlatformUtils::sendMessage, ModCommand::setKeyboard, ModConfig::save);
    }

}