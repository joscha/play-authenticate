package com.feth.play.module.pa.providers.oauth2.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.BasicOAuth2AuthUser;
import com.feth.play.module.pa.user.*;

public class GithubAuthUser extends BasicOAuth2AuthUser implements
        BasicIdentity, PicturedIdentity, ProfiledIdentity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static class Constants {
        public static final String LOCATION = "location"; // "Hamburg, Germany",
        public static final String HIREABLE = "hireable"; // false,
        public static final String PUBLIC_GISTS = "public_gists"; // 123,
        public static final String FOLLOWING_URL = "following_url"; // "https://api.github.com/users/joscha/following{/other_user}",
        public static final String URL = "url"; // "https://api.github.com/users/joscha",
        public static final String COMPANY = "company"; // "ACME",
        public static final String RECEIVED_EVENTS_URL = "received_events_url"; // "https://api.github.com/users/joscha/received_events",
        public static final String EVENTS_URL = "events_url"; // "https://api.github.com/users/joscha/events{/privacy}",
        public static final String UPDATED_AT = "updated_at"; // "2013-11-11T18:54:35Z",
        public static final String BIO = "bio"; // null,
        public static final String TYPE = "type"; // "User",
        public static final String ORGANIZATIONS_URL = "organizations_url"; // "https://api.github.com/users/joscha/orgs",
        public static final String SUBSCRIPTIONS_URL = "subscriptions_url"; // "https://api.github.com/users/joscha/subscriptions",
        public static final String REPOS_URL = "repos_url"; // "https://api.github.com/users/joscha/repos",
        public static final String STARRED_URL = "starred_url"; // "https://api.github.com/users/joscha/starred{/owner}{/repo}",
        public static final String GISTS_URL = "gists_url"; // "https://api.github.com/users/joscha/gists{/gist_id}",
        public static final String SITE_ADMIN = "site_admin"; // false,
        public static final String LOGIN = "login"; // "joscha",
        public static final String BLOG = "blog"; // "http://www.feth.com",
        public static final String PUBLIC_REPOS = "public_repos"; // 123,
        public static final String FOLLOWERS = "followers"; // 123,
        public static final String FOLLOWING = "following"; // 123,
        public static final String CREATED_AT = "created_at"; // "2010-01-23T00:19:18Z",
        public static final String GRAVATAR_ID = "gravatar_id"; // "3911f3dda74ddc708ea8f10566c0d8c3",
        public static final String FOLLOWERS_URL = "followers_url"; // "https://api.github.com/users/joscha/followers",
        public static final String ID = "id";
        public static final String EMAIL = "email";
        public static final String NAME = "name";
        public static final String HTML_URL = "html_url";
        public static final String AVATAR_URL = "avatar_url";
    }

    private String email;
    private String name;
    private String link;
    private String picture;
    private String bio;
    private String blog;
    private String company;
    private String login;
    private String gravatarId;
    private String location;

    public GithubAuthUser(final JsonNode n, final GithubAuthInfo info,
                          final String state) {
        super(n.get(Constants.ID).asText(), info, state);

        if (n.has(Constants.EMAIL)) {
            this.email = n.get(Constants.EMAIL).asText();
        }
        if (n.has(Constants.NAME)) {
            this.name = n.get(Constants.NAME).asText();
        }
        if (n.has(Constants.HTML_URL)) {
            this.link = n.get(Constants.HTML_URL).asText();
        }
        if (n.has(Constants.AVATAR_URL)) {
            this.picture = n.get(Constants.AVATAR_URL).asText();
        }
        if (n.has(Constants.BIO)) {
            this.bio = n.get(Constants.BIO).asText();
        }
        if (n.has(Constants.BLOG)) {
            this.blog = n.get(Constants.BLOG).asText();
        }
        if (n.has(Constants.COMPANY)) {
            this.company = n.get(Constants.COMPANY).asText();
        }
        if (n.has(Constants.LOGIN)) {
            this.login = n.get(Constants.LOGIN).asText();
        }
        if (n.has(Constants.GRAVATAR_ID)) {
            this.gravatarId = n.get(Constants.GRAVATAR_ID).asText();
        }
        if (n.has(Constants.LOCATION)) {
            this.location = n.get(Constants.LOCATION).asText();
        }
    }

    @Override
    public String getProvider() {
        return GithubAuthProvider.PROVIDER_KEY;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfileLink() {
        return link;
    }

    public String getPicture() {
        return picture;
    }

    public String getCompany() {
        return company;
    }

    public String getBlog() {
        return blog;
    }

    public String getBio() {
        return bio;
    }

    public String getLogin() {
        return login;
    }

    public String getGravatarId() {
        return gravatarId;
    }

    public String getLocation() {
        return location;
    }
}
