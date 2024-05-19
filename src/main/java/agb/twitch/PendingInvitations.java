package agb.twitch;

import java.util.HashSet;
import java.util.Set;

public class PendingInvitations {

    private static Set<String> pendingInvitationsUserIds = new HashSet<>();

    public static void add(String userId) {
        pendingInvitationsUserIds.add(userId);
    }

    public static boolean isUserPending(String userId) {
        return pendingInvitationsUserIds.contains(userId);
    }

    public static void remove(String userId) {
        pendingInvitationsUserIds.remove(userId);
    }

}
