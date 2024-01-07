package agb.twitch;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.Invite;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DiscordInvite {

    private static final String DISCORD_BOT_TOKEN = System.getenv("DISCORD_BOT_TOKEN");

    public static String createInvite() throws ExecutionException, InterruptedException, TimeoutException {

        System.out.println("Creating Discord Invite...");

        CompletableFuture<String> inviteLink = new CompletableFuture<>();

        DiscordClient client = DiscordClient.create(DISCORD_BOT_TOKEN);
        GatewayDiscordClient gateway = client.login().block();

        gateway.on(ReadyEvent.class).subscribe(event -> {

            try {

                TextChannel channel = (TextChannel) gateway.getChannelById(Snowflake.of("1030366695379959859")).block();

                if (channel != null) {
                    channel.createInvite(inviteSpec -> {
                        inviteSpec.setTemporary(true);
                        inviteSpec.setMaxAge(30); // 30 seconds
                        inviteSpec.setMaxUses(1); // 1 use
                    }).map(Invite::getCode).subscribe(code -> {
                        System.out.println("Generated Discord Invite Code: " + code);
                        inviteLink.complete("https://discord.com/invite/" + code);
                    });
                }
            } catch (Exception ex) {
                System.out.println("Error creating Discord Invite: " + ex.getMessage());
                ex.printStackTrace();
            }

        });

        gateway.logout().block();
        gateway.onDisconnect().block();

        return inviteLink.get(12, TimeUnit.SECONDS);

    }
}
