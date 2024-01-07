package agb.twitch;

public enum OAuthScope {
    ANALYTICS_READ_EXTENSIONS("analytics:read:extensions"),
    ANALYTICS_READ_GAMES("analytics:read:games"),
    BITS_READ("bits:read"),
    CHANNEL_MANAGE_ADS("channel:manage:ads"),
    CHANNEL_READ_ADS("channel:read:ads"),
    CHANNEL_MANAGE_BROADCAST("channel:manage:broadcast"),
    CHANNEL_READ_CHARITY("channel:read:charity"),
    CHANNEL_EDIT_COMMERCIAL("channel:edit:commercial"),
    CHANNEL_READ_EDITORS("channel:read:editors"),
    CHANNEL_MANAGE_GUEST_STAR("channel:manage:guest_star"),
    CHANNEL_READ_HYPE_TRAIN("channel:read:hype_train"),
    CHANNEL_MANAGE_MODERATORS("channel:manage:moderators"),
    CHANNEL_READ_POOLS("channel:read:pools"),
    CHANNEL_READ_PREDICTIONS("channel:read:predictions"),
    CHANNEL_MANAGE_PREDICTIONS("channel:manage:predictions"),
    CHANNEL_MANAGE_RAIDS("channel:manage:raids"),
    CHANNEL_READ_REDEMPTIONS("channel:read:redemptions"),
    CHANNEL_MANAGE_REDEMPTIONS("channel:manage:redemptions"),
    CHANNEL_MANAGE_SCHEDULE("channel:manage:schedule"),
    CHANNEL_READ_STREAM_KEY("channel:read:stream_key"),
    CHANNEL_MANAGE_VIDEOS("channel:manage:videos"),
    CHANNEL_READ_VIPS("channel:read:vips"),
    CHANNEL_MANAGE_VIPS("channel:manage:vips"),
    CLIPS_EDIT("clips:edit"),
    MODERATION_READ("moderation:read"),
    MODERATOR_MANAGE_ANNOUNCEMENTS("moderator:manage:announcements"),
    MODERATOR_MANAGE_AUTOMOD("moderator:manage:automod"),
    MODERATOR_READ_AUTOMOD_SETTINGS("moderator:read:automod_settings"),
    MODERATOR_MANAGE_AUTOMOD_SETTINGS("moderator:manage:automod_settings"),
    MODERATOR_MANAGE_BANNED_USER("moderator:manage:banned_users"),
    MODERATOR_READ_BLOCKED_TERMS("moderator:read:blocked_terms"),
    MODERATOR_MANAGE_BLOCKED_TERMS("moderator:manage:blocked_terms"),
    MODERATOR_MANAGE_CHAT_MESSAGES("moderator:manage:chat_messages"),
    MODERATOR_READ_CHAT_SETTINGS("moderator:read:chat_settings"),
    MODERATOR_MANAGE_CHAT_SETTINGS("moderator:manage:chat_settings"),
    MODERATOR_READ_CHATTERS("moderator:read:chatters"),
    MODERATOR_READ_FOLLOWERS("moderator:read:followers"),
    MODERATOR_READ_GUEST_STAR("moderator:read:guest_star"),
    MODERATOR_MANAGE_GUEST_STAR("moderator:manage:guest_star"),
    MODERATOR_READ_SHIELD_MODE("moderator:read:shield_mode"),
    MODERATOR_MANAGE_SHIELD_MODE("moderator:manage:shield_mode"),
    MODERATOR_READ_SHOUTOUTS("moderator:read:shoutouts"),
    MODERATOR_MANAGE_SHOUTOUTS("moderator:manage:shoutouts"),
    USER_EDIT("user:edit"),
    USER_EDIT_FOLLOWS("user:edit:follows"),
    USER_READ_BLOCKED_USERS("user:read:blocked_users"),
    USER_MANAGE_BLOCKED_USERS("user:manage:blocked_users"),
    USER_READ_BROADCAST("user:read:broadcast"),
    USER_MANAGE_CHAT_COLOR("user:manage:chat_color"),
    USER_READ_EMAIL("user:read:email"),
    USER_READ_FOLLOWS("user:read:follows"),
    USER_READ_SUBSCRIPTIONS("user:read:subscriptions"),
    USER_MANAGE_WHISPERS("user:manage:whispers"),
    CHANNEL_BOT("channel:bot"),
    CHANNEL_MODERATE("channel:moderate"),
    CHAT_EDIT("chat:edit"),
    CHAT_READ("chat:read"),
    USER_BOT("user:bot"),
    USER_READ_CHAT("user:read:chat"),
    WHISPERS_READ("whispers:read"),
    WHISPERS_EDIT("whispers:edit");

    private final String scope;

    OAuthScope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return this.scope;
    }
}
