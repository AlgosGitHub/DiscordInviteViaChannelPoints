package agb.twitch;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.CustomReward;
import com.github.twitch4j.helix.domain.CustomRewardList;

public class Main {

    public static void main(String[] args) {

        //check for arg -create "raffle name", and if present: create a new reward with the given name

        if(args.length > 0) {
            if(args[0].equals("-create")) {
                if(args.length > 1) {

                    String rewardName = args[1];

                    createRewardWithName(rewardName);

                }
            } else if(args[0].equals("-listen")) {
                if(args.length > 1) {

                    String rewardId = args[1];

                    listenForRedemptionsOfRewardId(rewardId);

                }
            }
        }

    }

    private static void listenForRedemptionsOfRewardId(String rewardId) {

        // start the OAuth endpoint to catch the callback when our user logs-in with their authorized twitch account.
        new TwitchOAuthEndpoint(
            authorizedModerator -> new PointRedemptionListener(authorizedModerator, rewardId)
        ).start();

    }

    private static void createRewardWithName(String rewardName) {

        System.out.println("By your command, Creating Reward ("+rewardName+")...");

        // start the OAuth endpoint to catch the callback when our user logs-in with their authorized twitch account.
        new TwitchOAuthEndpoint(authorizedModerator -> {

            try {

                TwitchClient twitchClient = authorizedModerator.getTwitchClient();
                String authToken = authorizedModerator.getCredential().getAccessToken();
                String broadcasterId = authorizedModerator.getUser().getId();

                System.out.println("Sending API Call to create reward ("+rewardName+") for channel (" + authorizedModerator.getUser().getDisplayName() + ")");

                CustomRewardList rewardsCreated = twitchClient.getHelix().createCustomReward(authToken, broadcasterId, new CustomReward().withTitle(rewardName).withPrompt("Insert Prompt").withCost(1000000).withIsEnabled(false)).execute();

                rewardsCreated.getRewards().forEach(reward -> {

                    System.out.println("Reward Created: " + reward.getTitle() + " / " + reward.getId());

                    System.out.println("Starting Point Redemption Listener for Reward ("+reward.getTitle()+")");

                    PointRedemptionListener pointRedemptionListener = new PointRedemptionListener(authorizedModerator, reward.getId());

                });

            } catch (Exception e) {

                e.printStackTrace();

            }

        }).start();

    }

}