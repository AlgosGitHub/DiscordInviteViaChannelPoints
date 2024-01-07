package agb.twitch;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import org.apache.http.client.utils.URIBuilder;

import java.util.ArrayList;
import java.util.Objects;

public class TwitchAuthService {

    static final String REDIRECT_URL = "https://discord.algobro.io/auth_callback";
    static final String TWITCH_LOGIN_REDIRECT = "https://discord.algobro.io/twitch_login";
    public static String APP_CLIENT_ID;
    public static String APP_CLIENT_SECRET;
    public static String KEYSTORE_PASSWORD;

    public TwitchAuthService() {
        try {
            loadEnv();

            // generate the link for our streamer to click
            userOAuthURL(new ArrayList<>() {{
                add(OAuthScope.CHANNEL_MANAGE_REDEMPTIONS);
                add(OAuthScope.CHANNEL_READ_REDEMPTIONS);
            }}, REDIRECT_URL);

            // generate the login with twitch link for our users to click
            userOAuthURL(new ArrayList<>() {{
                add(OAuthScope.USER_READ_EMAIL);
            }}, TWITCH_LOGIN_REDIRECT);
        } catch (Exception e) {
            System.out.println("Error loading environment variables: " + e.getMessage());
        }
    }

    private void loadEnv() throws Exception {
        APP_CLIENT_ID = System.getenv("DISCORD_TWITCH_APP_ID");
        APP_CLIENT_SECRET = System.getenv("DISCORD_TWITCH_APP_SECRET");
        if (Objects.equals(APP_CLIENT_ID, "")) {
            throw new Exception("APP_ID not found in environment variables");
        }
        if (Objects.equals(APP_CLIENT_SECRET, "")) {
            throw new Exception("APP_SECRET not found in environment variables");
        }
        KEYSTORE_PASSWORD = System.getenv("DISCORD_KEYSTORE_PASSWORD");
        if (Objects.equals(KEYSTORE_PASSWORD, "")) {
            throw new Exception("KEYSTORE_PASSWORD not found in environment variables");
        }
    }

    private void userOAuthURL(ArrayList<OAuthScope> scopes, String redirectUrl) {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("https").setHost("id.twitch.tv").setPath("/oauth2/authorize")
                .addParameter("response_type", "code")
                .addParameter("client_id", APP_CLIENT_ID)
                .addParameter("scope", scopes.stream().map(OAuthScope::getScope).reduce((a, b) -> a + " " + b).orElse(""))
                .addParameter("redirect_uri", redirectUrl);

        String url = uriBuilder.toString();
        System.out.println("OAuthURL: " + url);
    }

    public AuthorizedModerator authenticate(String code) {

        System.out.println("Authenticating Twitch Channel Moderator...");

        TwitchIdentityProvider twitchIdentityProvider = new TwitchIdentityProvider(APP_CLIENT_ID, APP_CLIENT_SECRET, REDIRECT_URL);

        try {

            // the Credential manager keeps our auth tokens fresh.
            OAuth2Credential credentials = twitchIdentityProvider.getCredentialByCode(code);
            CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
            credentialManager.registerIdentityProvider(twitchIdentityProvider);

            // this is where the magic happens.
            TwitchClient twitchClient = TwitchClientBuilder.builder()
                    .withClientId(APP_CLIENT_ID)
                    .withClientSecret(APP_CLIENT_SECRET)
                    .withChatAccount(credentials)
                    .withEnableChat(true)
                    .withEnableHelix(true)
                    .withCredentialManager(credentialManager)
                    .withEnablePubSub(true)
                    .build();

            // get the authenticated user's details, we'll need the user-name for IRC chat.
            String accessToken = credentials.getAccessToken();
            UserList resultList = twitchClient.getHelix().getUsers(accessToken, null, null).execute();

            // get the first result, there should only be one.
            User user = resultList.getUsers().getFirst();

            System.out.println("Twitch Channel Mod Authenticated: " + user.getDisplayName());
            System.out.println("\t`-> Account Created at: " + user.getCreatedAt());


            AuthorizedModerator authorizedModerator = new AuthorizedModerator();

            authorizedModerator.setUser(user);
            authorizedModerator.setTwitchClient(twitchClient);
            authorizedModerator.setCredential(credentials);

            return authorizedModerator;

        } catch (RuntimeException e) {
            System.out.println("Error during authentication: " + e.getMessage());
        }

        // todo: more elegant error handling.
        throw new RuntimeException("Failed to authenticate user.");
    }

}
