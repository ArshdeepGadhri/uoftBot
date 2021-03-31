package me.arsh.uoftBot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class electivesCommand extends Command {

    EventWaiter waiter = null;

    public electivesCommand(EventWaiter waiter){
        this.name = "elective";
        this.waiter = waiter;
        this.aliases = new String[]{"electives","e","ele","elec","soc","hum","sci"};
        this.cooldown = 5;
        this.help = "Returns Course list by elective by prereq";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.usesTopicTags = false;
    }
    @Override
    protected void execute(CommandEvent commandEvent) {

        if (commandEvent.getArgs().length()==0||commandEvent.getArgs().split(" ").length==1){
            commandEvent.reply("Missing args, you need to provide me with a Campus [UTSG, UTM, UTSC] and a page [#]\nExample uoftBot e UTM 1");
            return;
        }

        EmbedBuilder campus = new EmbedBuilder();
        campus.setTitle("Select an Elective");
        campus.setDescription("```nimrod\n[1] Social Science\n[2] Humanities\n[3] Science```");
        campus.setThumbnail("https://i.redd.it/s3soe6084z041.png");
        campus.setColor(new Color(0,46,100));
        AtomicReference<String> camp = new AtomicReference<>("");
        commandEvent.getChannel().sendMessage(campus.build()).queue(m -> {
            m.addReaction("1️⃣").queue(); //a
            m.addReaction("2️⃣").queue(); //b
            m.addReaction("3️⃣").queue(); //c
            waiter.waitForEvent(MessageReactionAddEvent.class,
                    event -> {
                        MessageReaction.ReactionEmote emote = event.getReactionEmote();
                        return (!Objects.requireNonNull(event.getUser()).isBot() && event.getUser().equals(event.getUser()) && ("1️⃣".equals(emote.getName()) || "2️⃣".equals(emote.getName()) || "3️⃣".equals(emote.getName()) ) );
                    },
                    event -> {
                        m.delete().queue();
                        MessageReaction.ReactionEmote emote = event.getReactionEmote();
                        if ("2️⃣".equals(emote.getName())) { //b
                            camp.set("hum");
                        }else if ("3️⃣".equals(emote.getName())){ //c
                            camp.set("sci");
                        } else { //a
                            camp.set("soc");
                            //  cam = "&campus=geo";
                        }
                        try{
                            HttpClient client = HttpClientBuilder.create().build();
                            HttpGet req = null;
                            String[] arg = commandEvent.getArgs().split(" ");
                            if (arg[0].equalsIgnoreCase("utsg")){
                                req = new HttpGet("https://nikel.ml/api/courses?arts_and_science_distribution="+camp.get()+"&limit=10&offset="+(Integer.parseInt(arg[1])*10));
                            }else if(arg[0].equalsIgnoreCase("utm")){
                                req = new HttpGet("https://nikel.ml/api/courses?utm_distribution="+camp.get()+"&limit=10&offset="+(Integer.parseInt(arg[1])*10));
                            }else{
                                req = new HttpGet("https://nikel.ml/api/courses?utsc_breadth="+camp.get()+"&limit=10&offset="+(Integer.parseInt(arg[1])*10));
                            }

                            HttpResponse response = client.execute(req);

                            BufferedReader buff = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                            String line;
                            StringBuilder def = new StringBuilder();
                            while ((line=buff.readLine())!=null) {
                                def.append(line).append("\n");

                            }
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setFooter("Page: "+arg[1]);
                            eb.setThumbnail("https://i.redd.it/s3soe6084z041.png");
                            eb.setColor(new Color(0,46,100));
                            JsonObject jsonO = new JsonParser().parse(def.toString()).getAsJsonObject();
                            if (jsonO.get("response").getAsJsonArray().size()==0){
                                commandEvent.reply("> Wasn't able to find any data");
                                return;
                            }
                            eb.setTitle("List of possible Electives | "+camp.get());
                            JsonArray jsonArray = jsonO.get("response").getAsJsonArray();
                            for (int i = 0; i < jsonO.get("response").getAsJsonArray().size(); i++){
                                String title = jsonArray.get(i).getAsJsonObject().get("name").getAsString();
                                String code = jsonArray.get(i).getAsJsonObject().get("code").getAsString();
                                String cmp = jsonArray.get(i).getAsJsonObject().get("term").getAsString();
                                String dep = jsonArray.get(i).getAsJsonObject().get("department").getAsString();
                                eb.addField(title + " | " + code,"```fix\nDepartment: "+dep+"\nTerm: "+cmp+"```",false);
                            }
                            commandEvent.reply(eb.build());

                        }catch (Exception i){
                            i.printStackTrace();
                            commandEvent.reply("> Something went wrong");
                        }

                    },30, TimeUnit.SECONDS, () -> {
                        m.addReaction("\uD83D\uDED1").queue();
                        m.clearReactions().queue();
                        campus.setDescription(commandEvent.getAuthor().getAsTag()+" took too long to react");
                        m.editMessage(campus.build()).queue();
                    });
        });
    }
}
