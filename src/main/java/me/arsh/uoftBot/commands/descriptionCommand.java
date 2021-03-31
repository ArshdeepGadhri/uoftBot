package me.arsh.uoftBot.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class descriptionCommand extends Command {
    EventWaiter waiter;
    public descriptionCommand(EventWaiter waiter){
        this.waiter = waiter;
        this.name = "description";
        this.aliases = new String[]{"d","desc"};
        this.cooldown = 5;
        this.help = "Returns Course data by description";
        this.guildOnly = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_WRITE};
        this.usesTopicTags = false;
    }
    @Override
    protected void execute(CommandEvent commandEvent) {
        if (commandEvent.getArgs().length()==0){
            commandEvent.reply("Missing args, you need to provide me with keywords");
            return;
        }
        String courseCode = commandEvent.getArgs().replace(" ","&description=").replace(",","");
        EmbedBuilder campus = new EmbedBuilder();
        campus.setTitle("Select a Campus");
        campus.setDescription("```nimrod\n[1] UTSG\n[2] UTM\n[3] UTSC```");
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
                            camp.set("&campus=mis");
                        }else if ("3️⃣".equals(emote.getName())){ //c
                            camp.set("&campus=sca");
                        } else { //a
                            camp.set("&campus=geo");
                            //  cam = "&campus=geo";
                        }
                        try{
                            HttpClient client = HttpClientBuilder.create().build();

                            HttpGet req = new HttpGet("https://nikel.ml/api/courses?description="+courseCode+camp.get());

                            HttpResponse response = client.execute(req);

                            BufferedReader buff = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                            String line;
                            StringBuilder def = new StringBuilder();
                            while ((line=buff.readLine())!=null) {
                                def.append(line).append("\n");

                            }
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setThumbnail("https://i.redd.it/s3soe6084z041.png");
                            eb.setColor(new Color(0,46,100));
                            JsonObject jsonO = new JsonParser().parse(def.toString()).getAsJsonObject();
                            if (jsonO.get("response").getAsJsonArray().size()==0){
                                commandEvent.reply("> Wasn't able to find any data for this specific course. It may not exist");
                                return;
                            }
                            pageZero(eb,jsonO);
                            commandEvent.getChannel().sendMessage(eb.build()).queue(e -> {
                                e.addReaction("\uD83D\uDC48\uD83C\uDFFB").queue();
                                e.addReaction("\uD83D\uDC49\uD83C\uDFFB").queue();
                                initWaiter(e.getIdLong(), e, commandEvent.getAuthor(), 0, commandEvent, eb, jsonO);
                            });

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
    private void initWaiter(long messageId, Message message, User user, int page, CommandEvent e, EmbedBuilder eb, JsonObject jsonO){ ;
        waiter.waitForEvent(MessageReactionAddEvent.class,
                event -> {
                    MessageReaction.ReactionEmote emote = event.getReactionEmote();
                    return (!user.isBot() && messageId == event.getMessageIdLong() && user.equals(event.getUser()) && ("\uD83D\uDC48\uD83C\uDFFB".equals(emote.getName())||"\uD83D\uDC49\uD83C\uDFFB".equals(emote.getName())));
                },
                event -> {
                    MessageReaction.ReactionEmote emote = event.getReactionEmote();
                    message.removeReaction(emote.getName(), user).queue();
                    if ("\uD83D\uDC48\uD83C\uDFFB".equals(emote.getName())){ //left
                        if (page==0){
                            eb.clearFields();
                            pageSix(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 5, e, eb, jsonO);
                        }else if (page==1){
                            eb.clearFields();
                            pageZero(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 0, e, eb, jsonO);
                        }else if (page==2) {
                            eb.clearFields();
                            pageOne(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 1, e, eb, jsonO);
                        }else if (page==3){
                            eb.clearFields();
                            pageTwo(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 2, e, eb, jsonO);
                        }else if (page==4){
                            eb.clearFields();
                            pageFour(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 3, e, eb, jsonO);
                        }else if (page==5){
                            eb.clearFields();
                            pageFive(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 4, e, eb, jsonO);
                        }
                    }
                    else {
                        if (page==0){
                            eb.clearFields();
                            pageOne(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 1, e, eb, jsonO);
                        }else if (page==1){
                            eb.clearFields();
                            pageTwo(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 2, e, eb, jsonO);
                        }else if (page==2) {
                            eb.clearFields();
                            pageFour(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 3, e, eb, jsonO);
                        }else if (page==3) {
                            eb.clearFields();
                            pageFive(eb, jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 4, e, eb, jsonO);
                        }else if (page==4){
                            eb.clearFields();
                            pageSix(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 5, e, eb, jsonO);
                        }
                        else if (page==5){
                            eb.clearFields();
                            pageZero(eb,jsonO);
                            message.editMessage(eb.build()).queue();
                            initWaiter(messageId, message, user, 0, e, eb, jsonO);
                        }
                    }
                },2, TimeUnit.MINUTES, () -> {
                    message.addReaction("\uD83D\uDED1").queue();
                    message.clearReactions().queue();
                });
    }

    private void pageZero(EmbedBuilder eb, JsonObject data){
        try {
            JsonObject json = data.get("response").getAsJsonArray().get(0).getAsJsonObject();
            eb.setTitle(json.get("name").getAsString()+" | "+json.get("code").getAsString(),"https://coursefinder.utoronto.ca/course-search/search/courseInquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId="+json.get("id").getAsString());
            eb.setDescription("```"+json.get("description").getAsString()+"```");
            String published = json.get("last_updated").getAsString();
            LocalDateTime dateTime = LocalDateTime.parse(published);
            eb.setTimestamp(dateTime);
            eb.setFooter("Page 1 - Data Last updated ");
        }catch (Exception ignored){}
    }
    private void pageOne(EmbedBuilder eb, JsonObject data){
        try {
            JsonObject json = data.get("response").getAsJsonArray().get(0).getAsJsonObject();
            eb.setDescription("");
            eb.addField("Level","```\n"+json.get("level").getAsString()+"```",true);
            eb.addField("Data for Term","```\n"+json.get("term").getAsString()+"```",true);
            eb.addField("Data for Campus","```\n"+json.get("campus").getAsString()+"```",true);
            eb.addField("Division","```\n"+json.get("division").getAsString()+"```",true);
            eb.addField("Department","```\n"+json.get("department").getAsString()+"```",true);
            eb.setFooter("Page 2 - Data Last updated ");
        }catch (Exception ignored){}
    }
    private void pageTwo(EmbedBuilder eb, JsonObject data){
        try {
            JsonObject json = data.get("response").getAsJsonArray().get(0).getAsJsonObject();
            eb.setDescription("Other Details");
            try {
                eb.addField("PreReqs", "```\n" + json.get("prerequisites").getAsString() + "```", false);
            }catch (Exception ignored){}
            try {
                eb.addField("CoReqs","```\n"+json.get("corequisites").getAsString()+"```",false);
            }catch (Exception ignored){}

            try{
                eb.addField("Recommended Prep","```\n"+json.get("recommended_preparation").getAsString()+"```",false);
            }catch (Exception ignored){}
            try {
                eb.addField("Exclusions","```\n"+json.get("exclusions").getAsString()+"```",false);
            }catch (Exception ignored){}
            try {
                eb.addField("Distribution", "```\nArtSci Breadth: " + json.get("arts_and_science_breadth").toString()  + "\nArtSci Distribution: " + json.get("arts_and_science_distribution").toString() + "\nUtm Distribution: " + json.get("utm_distribution").toString() + "\nUtsc Breadth: " + json.get("utsc_breadth").toString() + "\nAPSC electives: " + json.get("apsc_electives").toString() + "```", false);
            }catch (Exception ignored){}
            eb.setFooter("Page 3 - Data Last updated ");
        }catch (Exception ignored){}
    }
    private void pageFour(EmbedBuilder eb, JsonObject data){
        try {
            eb.setDescription("");
            JsonObject json = data.get("response").getAsJsonArray().get(0).getAsJsonObject();
            int slots = json.get("meeting_sections").getAsJsonArray().size();
            eb.setDescription("Lectures");
            for (int j = 0; j < slots; j++) {
                JsonObject slot1 = json.get("meeting_sections").getAsJsonArray().get(j).getAsJsonObject();
                String timings = "";
                for (int i = 0; i < slot1.get("times").getAsJsonArray().size(); i++) {
                    String day = slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("day").getAsString();
                    timings += "```coffee\nDay: " + day.substring(0,1).toUpperCase()+day.substring(1) +
                            "\nStart: " + Long.parseLong(slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("start").toString())/3600+":00" +
                            "\nEnd: " + Long.parseLong(slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("end").toString())/3600+":00" + "```";

                }
                String code = slot1.get("code").getAsString();
                if (code.contains("Lec")) {
                    eb.addField("Lecture: " + code + " - Class Size: " + slot1.get("size"), timings+"------------------------\n", true);
                }
            }
            eb.setFooter("Page 4 - Data Last updated ");
        }catch (Exception ignored){ignored.printStackTrace();}
    }
    private void pageFive(EmbedBuilder eb, JsonObject data){
        try {
            eb.setDescription("");
            JsonObject json = data.get("response").getAsJsonArray().get(0).getAsJsonObject();
            int slots = json.get("meeting_sections").getAsJsonArray().size();
            eb.setDescription("Tutorials");
            for (int j = 0; j < slots; j++) {
                JsonObject slot1 = json.get("meeting_sections").getAsJsonArray().get(j).getAsJsonObject();
                String timings = "";
                for (int i = 0; i < slot1.get("times").getAsJsonArray().size(); i++) {
                    String day = slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("day").getAsString();
                    timings += "```coffee\nDay: " + day.substring(0,1).toUpperCase()+day.substring(1) +
                            "\nStart: " + Long.parseLong(slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("start").toString())/3600+":00" +
                            "\nEnd: " + Long.parseLong(slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("end").toString())/3600+":00" + "```";

                }
                String code = slot1.get("code").getAsString();
                if (code.contains("Tut")) {
                    eb.addField("Tutorial: " + code + " - Class Size: " + slot1.get("size"), timings + "------------------------\n", true);
                }
            }
            eb.setFooter("Page 5 - Data Last updated ");
        }catch (Exception ignored){ignored.printStackTrace();}
    }
    private void pageSix(EmbedBuilder eb, JsonObject data){
        try {
            eb.setDescription("");
            JsonObject json = data.get("response").getAsJsonArray().get(0).getAsJsonObject();
            int slots = json.get("meeting_sections").getAsJsonArray().size();
            eb.setDescription("Practicals");
            for (int j = 0; j < slots; j++) {
                JsonObject slot1 = json.get("meeting_sections").getAsJsonArray().get(j).getAsJsonObject();
                String timings = "";
                for (int i = 0; i < slot1.get("times").getAsJsonArray().size(); i++) {
                    String day = slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("day").getAsString();
                    timings += "```coffee\nDay: " + day.substring(0,1).toUpperCase()+day.substring(1) +
                            "\nStart: " + Long.parseLong(slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("start").toString())/3600+":00" +
                            "\nEnd: " + Long.parseLong(slot1.get("times").getAsJsonArray().get(i).getAsJsonObject().get("end").toString())/3600+":00" + "```";

                }
                String code = slot1.get("code").getAsString();

                if (code.contains("Pra")){
                    eb.addField("Practical: " + code + " - Class Size: " + slot1.get("size"), timings+"------------------------\n", true);
                }
            }
            eb.setFooter("Page 6 - Data Last updated ");
        }catch (Exception ignored){ignored.printStackTrace();}
    }
}
