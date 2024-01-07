package agb.twitch;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import spark.Spark;

import java.util.function.Consumer;

import static agb.twitch.TwitchAuthService.*;

public class TwitchOAuthEndpoint {

    private static Consumer<AuthorizedModerator> authorizationComplete;

    public TwitchOAuthEndpoint(Consumer<AuthorizedModerator> authorizationComplete) {
        this.authorizationComplete = authorizationComplete;
    }

    protected void start() {

        System.out.println("Starting Twitch OAuth2 Callback Endpoint...");

        TwitchAuthService authService = new TwitchAuthService();

        // Create a Spark instance
        Spark.port(443); // You can change the port as needed

        // enable https
        Spark.secure("keystore.jks", KEYSTORE_PASSWORD, null, null);

        // Define a dynamic endpoint that executes the associated runnable
        Spark.get("/:endpointName", (request, response) -> {

            // extract the name of the endpoint hit by this request i.e. url.com/endpointName
            String endpointName = request.params(":endpointName");

            // sanity check: does this endpoint exist in our map of endpoints?
            if (endpointName.equals("auth_callback")) {

                // log the call & response
                String code = request.queryParams("code");

                System.out.println("Auth Completed. OAuth2 Token: " + code.substring(code.length()-4));

                try {

                    AuthorizedModerator authMod = authService.authenticate(code);

                    authorizationComplete.accept(authMod);

                } catch (Exception e) {

                    e.printStackTrace();

                    response.status(500);

                    return "Internal Server Error";
                }

                // return the response, this will display in the browser
                return "Authentication Complete. Thank you for your cooperation. Have a nice day.";

            }

            else if (endpointName.equals("twitch_login")) {

                // log the call & response
                String code = request.queryParams("code");

                System.out.println("Auth Completed. OAuth2 Token: " + code.substring(code.length()-4));

                TwitchIdentityProvider twitchIdentityProvider = new TwitchIdentityProvider(APP_CLIENT_ID, APP_CLIENT_SECRET, TWITCH_LOGIN_REDIRECT);

                try {

                    // the Credential manager keeps our auth tokens fresh.
                    OAuth2Credential credentials = twitchIdentityProvider.getCredentialByCode(code);
                    CredentialManager credentialManager = CredentialManagerBuilder.builder().build();
                    credentialManager.registerIdentityProvider(twitchIdentityProvider);

                    // the user's access token can be used to pull their user-name & email address
                    String accessToken = credentials.getAccessToken();

                    UserList resultList = AuthorizedModerator.getTwitchClient().getHelix().getUsers(accessToken, null, null).execute();

                    // get the first result, there should only be one.
                    User user = resultList.getUsers().getFirst();

                    System.out.println("Twitch User Login: " + user.getDisplayName());

                    if(PendingInvitations.isUserPending(user.getId())) {
                        System.out.println("User is expected. Redirecting to Discord Invite...");
                        PendingInvitations.remove(user.getId());

                        // create a discord invite that's one-time-use and expires in 90 seconds.
                        String inviteUrl = DiscordInvite.createInvite();

                        // redirect to the invite.
                        response.redirect(inviteUrl, 302);

                        return "Redirecting to Discord Invite...";

                    } else {
                        System.out.println("User is not expected. Returning 403...");
                        response.status(403);
                        return "Use channel points to unlock an invitation. Then refresh this page.";
                    }

                } catch (Exception e) {

                    e.printStackTrace();

                    response.status(500);

                    return "Internal Server Error";
                }

                // return the response, this will display in the browser

            }
            else {

                response.status(418);

                return "I am a tea pot.";

            }
        });


    }
}
