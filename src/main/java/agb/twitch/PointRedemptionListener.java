package agb.twitch;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.helix.domain.CustomRewardRedemptionList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.RedemptionStatusUpdateEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.netflix.hystrix.HystrixCommand;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PointRedemptionListener {

    private final AuthorizedModerator authorizedModerator;
    private final String rewardId;
    private List<RewardRedeemedEvent> eventQueue = new ArrayList<>();

    public PointRedemptionListener(AuthorizedModerator authorizedModerator, String rewardId) {

        this.authorizedModerator = authorizedModerator;
        this.rewardId = rewardId;

        subscribeToChannelPointRedemptions();

    }


    private void subscribeToChannelPointRedemptions() {

        System.out.println("Subscribing to Channel Point Redemptions");

        TwitchClient client = authorizedModerator.getTwitchClient();

        EventManager eventManager = client.getEventManager();

        eventManager.onEvent(RewardRedeemedEvent.class, redemptionEvent -> {
            try {

                System.out.println("Adding Redemption Event to Queue");
                synchronized (eventQueue) {

                    // ignore any redemptions that are not for the raffle reward
                    if (!redemptionEvent.getRedemption().getReward().getId().equals(rewardId))
                        return;

                    eventQueue.add(redemptionEvent);

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // register event handler
        eventManager.onEvent(RedemptionStatusUpdateEvent.class, this::handleFulfillmentEvent);

        // subscribe to the channel, presumed to be the authorized moderator's channel
        client.getPubSub().listenForChannelPointsRedemptionEvents(authorizedModerator.getCredential(), authorizedModerator.getUser().getId());

        // start event queue handler
        Thread.startVirtualThread(() -> {
            try {

                System.out.println("Starting Event Queue Handler");
                while(true) {
                    synchronized (eventQueue) {
                        if(!eventQueue.isEmpty()) {
                            handleRedemptionEvent(eventQueue.removeFirst());
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

    }

    private void handleRedemptionEvent(RewardRedeemedEvent event) {

        // get the event details
        ChannelPointsRedemption redemption = event.getRedemption();

        // print the event details
        System.out.println("User with ID " + redemption.getUser().getId() + " redeemed " + redemption.getReward().getTitle() + ". Status: " + redemption.getStatus());

        // handle the initial redemption event by fulfilling it!
        if (redemption.getStatus().equals(RedemptionStatus.UNFULFILLED.toString())) {

            handleUnfulfilledPointRedemption(redemption);

        }

    }

    private void handleUnfulfilledPointRedemption(ChannelPointsRedemption event) {

        String
            channelId  =  event.getChannelId(),
            eventId    =  event.getId(),
            userName   =  event.getUser().getDisplayName(),
            userId     =  event.getUser().getId();

        // validate account age
        if(isAccountOldEnough(userId)) {

            System.out.println("User is old enough to enter: " + userName);

            fulfillPointRedemption(channelId, eventId, userId);

        } else {

            System.out.println("Cannot Redeem Channel Point Redemption because "+userName+" is not old enough!");

            rejectPointRedemption(channelId, eventId);

        }

    }

    private void fulfillPointRedemption(String channelId, String redemptionEventId, String userId) {

        try {

            System.out.println("Fulfilling redemption...");

            String authToken = authorizedModerator.getCredential().getAccessToken();

            HystrixCommand<CustomRewardRedemptionList> customRewardRedemptionListHystrixCommand = authorizedModerator.getTwitchClient().getHelix().updateRedemptionStatus(
                    authToken, // Ensure this token has the right scope,
                    channelId,
                    rewardId,
                    List.of(redemptionEventId),
                    RedemptionStatus.FULFILLED
            );

            CustomRewardRedemptionList returns = customRewardRedemptionListHystrixCommand.execute();

            returns.getRedemptions().forEach(customRewardRedemption -> {
                System.out.println(customRewardRedemption.getUserName() + "'s " + customRewardRedemption.getReward().getTitle() + " was " + customRewardRedemption.getStatus());

                // add user to pending invitations
                PendingInvitations.add(userId);

            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void rejectPointRedemption(String channelId, String redemptionEventId) {

        try {

            System.out.println("Rejecting redemption...");
            HystrixCommand<CustomRewardRedemptionList> customRewardRedemptionListHystrixCommand = authorizedModerator
                    .getTwitchClient()
                    .getHelix()
                    .updateRedemptionStatus(
                authorizedModerator.getCredential().getAccessToken(), // Ensure this token has the right scope,
                channelId,
                rewardId,
                List.of(redemptionEventId),
                RedemptionStatus.CANCELED
            );

            CustomRewardRedemptionList returns = customRewardRedemptionListHystrixCommand.execute();

            returns.getRedemptions().forEach(customRewardRedemption -> System.out.println("Redemption Status: " + customRewardRedemption.getUserName() + "'s " + customRewardRedemption.getReward().getTitle() + " was " + customRewardRedemption.getStatus()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean isAccountOldEnough(String userId) {

        // get the authenticated user's details, we'll need the user-name for IRC chat.
        UserList resultList = authorizedModerator.getTwitchClient().getHelix().getUsers(null, List.of(userId), null).execute();

        // get the first result, there should only be one.
        User user = resultList.getUsers().getFirst();

        Instant createdAt = user.getCreatedAt();
        Instant minimumAccountAge = Instant.now().minus(30, ChronoUnit.DAYS);

        return createdAt.isBefore(minimumAccountAge);

    }

    private void handleFulfillmentEvent(RedemptionStatusUpdateEvent event) {

        System.out.println("Redemption Status Update Event Fired!");

        // get the event details
        ChannelPointsRedemption redemption = event.getRedemption();

    }

}
