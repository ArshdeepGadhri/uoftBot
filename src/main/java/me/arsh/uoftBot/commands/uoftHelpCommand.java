package me.arsh.uoftBot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class uoftHelpCommand extends Command {
    public uoftHelpCommand(){
        this.name = "help";
        this.aliases = new String[]{"helpme","documentation"};
        this.cooldown = 5;
        this.help = "Returns help page";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.usesTopicTags = false;
    }
    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Help Page");
        eb.setDescription("**To List The Commands do @uoftBot commands**");
        eb.setColor(new Color(0,46,100));
        final long duration = ManagementFactory.getRuntimeMXBean().getUptime();

        final long years = duration / 31104000000L;
        final long months = duration / 2592000000L % 12;
        final long days = duration / 86400000L % 30;
        final long hours = duration / 3600000L % 24;
        final long minutes = duration / 60000L % 60;
        final long seconds = duration / 1000L % 60;

        String uptime = (years == 0 ? "" : "**" + years + "** Years, ")
                + (months == 0 ? "" : "**" + months + "** Months, ")
                + (days == 0 ? "" : "**" + days + "** Days, ")
                + (hours == 0 ? "" : "**" + hours + "** Hours, ")
                + (minutes == 0 ? "" : "**" + minutes + "** Minutes, ")
                + (seconds == 0 ? "" : "**" + seconds + "** Seconds, ");

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = (long) (runtime.maxMemory()*0.000001);
        long mem = (long) ((runtime.totalMemory() - runtime.freeMemory())*0.000001);

        User owner = event.getJDA().getUserById("170718155772002304");
        assert owner != null;
        eb.setThumbnail(event.getSelfMember().getUser().getAvatarUrl());
        eb.addField("My invite Link!","[Invite](https://discord.com/api/oauth2/authorize?client_id=722908883554926623&permissions=387136&scope=bot)",true);
        eb.addField("Add My other Bot!","[Arty Bot](https://artybot12.github.io)",true);
        eb.addField("Check out my API!","[Country-to-Codes](https://country-to-code.herokuapp.com/)",true);

        eb.appendDescription("\nPrefix `@uoftBot ` or `uoftBot `\nA bot by "
                +owner.getAsTag()+"\nLibrary: DiscordJDA"+"\nPowered by nikel\nUptime: "
                +uptime.substring(0,uptime.length()-2).replace("**","")
                +"\nMem: "+mem+"mb/"+(maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory)
                +"mb\nPing: "+event.getJDA().getGatewayPing()
                +"ms\nProject Started On: July 16 2020, ~2 Hours Total");
        event.reply(eb.build());
    }
}
