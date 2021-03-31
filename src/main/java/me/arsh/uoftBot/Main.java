package me.arsh.uoftBot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.github.cdimascio.dotenv.Dotenv;
import me.arsh.uoftBot.commands.*;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Main {
    public static BufferedImage bg = null;
    public static BufferedImage bg2 = null;

    public static Font font = null;
    public static Font font2 = null;

    private Main() throws LoginException {
        Dotenv dotenv = Dotenv.configure()
                .directory("C:\\Users\\arshd\\eclipse-workspace\\uoftBot\\src\\main\\java\\me\\arsh\\uoftBot") // token dir here
                .filename(".env").load();
        String token = dotenv.get("TOKEN");

        assert token != null;
        final DefaultShardManager JDA2 = (DefaultShardManager) new DefaultShardManagerBuilder()
                .setToken(token)
                .setStatus(OnlineStatus.ONLINE)
                .build();

        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder builder = new CommandClientBuilder();

        builder.useHelpBuilder(false);
        builder.setAlternativePrefix("uoftBot ");
        builder.setHelpWord("1234567890");
        builder.setOwnerId("170718155772002304");
        builder.setEmojis(">>> \uD83D\uDE03", ">>> ⌛", ">>> \uD83E\uDD21");
        builder.setActivity(Activity.listening("@uoftBot help"));

        CommandClient client = builder.build();

        client.addCommand(new courseCodeCommand(waiter));
        client.addCommand(new courseNameCommand(waiter));
        client.addCommand(new prereqCommand(waiter));
        client.addCommand(new electivesCommand(waiter));
        client.addCommand(new descriptionCommand(waiter));
        client.addCommand(new uoftHelpCommand());
        client.addCommand(new uoftCommandsCommand());

        JDA2.addEventListener(client);
        JDA2.addEventListener(waiter);
    }



    public static void main(String[] args) throws LoginException, IOException, FontFormatException {
        //Load once
        URL url = new URL("https://i.imgur.com/tBLkxxQ.png");
        URL url2 = new URL("https://imgur.com/LZbvMcR.png");
        bg  = ImageIO.read(url);
        bg2 = ImageIO.read(url2);

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("BebasNeue-Regular.ttf");

        font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(30f);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);

        inputStream = Main.class.getClassLoader().getResourceAsStream("arial-bold.ttf");

        assert inputStream != null;
        font2 = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(31f);
        ge.registerFont(font2);

        long before = System.currentTimeMillis();
        new Main();
        inputStream.close();

        System.err.println("Loaded in "+ TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - before)+"s");
    }
}
