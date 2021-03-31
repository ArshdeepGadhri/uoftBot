package me.arsh.uoftBot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;

public class uoftCommandsCommand extends Command {
    public uoftCommandsCommand(){
        this.name = "commands";
        this.aliases = new String[]{"cmd","commandlist","command"};
        this.cooldown = 5;
        this.help = "Returns a list of commands";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.usesTopicTags = false;
    }
    @Override
    protected void execute(CommandEvent commandEvent) {

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command List");
        eb.setDescription("@uoftBot name [courseName]\nReturns course data eg: `@uoftBot n proofs`\n\n" +
                "@uoftBot code [courseCode]\nReturns course data eg: `@uoftBot c mat102`\n\n" +
                "@uoftBot desc [list: keywords]\nReturns course data eg: `@uoftBot d asian migration`\n\n" +
                "@uoftBot prereq [list: prereq courseCodes]\nReturns course data eg: `@uoftBot p csc148 mat102`\n\n" +
                "@uoftBot elective [campusName] [page#]\nReturns a list of electives eg: `@uoftBot e UTM 2`");
        eb.setColor(Color.pink);
        eb.addField("Other Commands","```diff\nuoftBot info \nuoftBot connect4 @user\nuoftBot anime [animeName]\nuoftBot ask [query]\nuoftBot profile @user```",false);

        User owner = commandEvent.getJDA().getUserById("170718155772002304");
        assert owner != null;
        eb.setFooter("A bot by "+owner.getAsTag(),owner.getAvatarUrl());
        eb.setThumbnail(commandEvent.getSelfMember().getUser().getAvatarUrl());
        commandEvent.reply(eb.build());
    }
}
