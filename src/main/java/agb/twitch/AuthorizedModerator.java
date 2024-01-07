package agb.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.User;

public class AuthorizedModerator {

    private static User user;

    private static OAuth2Credential credential;

    private static TwitchClient twitchClient;

    public static TwitchClient getTwitchClient() {
        return twitchClient;
    }

    public static void setTwitchClient(TwitchClient twitchClient) {
        AuthorizedModerator.twitchClient = twitchClient;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        AuthorizedModerator.user = user;
    }

    public static OAuth2Credential getCredential() {
        return credential;
    }

    public static void setCredential(OAuth2Credential credential) {
        AuthorizedModerator.credential = credential;
    }
}
