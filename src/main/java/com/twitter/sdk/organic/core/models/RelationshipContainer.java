package com.twitter.sdk.organic.core.models;

import com.google.gson.annotations.SerializedName;

public class RelationshipContainer {

    public static class Relationship {
        public final Target target;
        public final Source source;

        public Relationship(Target target, Source source) {
            this.target = target;
            this.source = source;
        }
    }

    public static class Party {
        @SerializedName("id")
        public final long id;

        @SerializedName("id_str")
        public final String idStr;

        @SerializedName("screen_name")
        public final String screenName;

        @SerializedName("following")
        public final boolean following;

        @SerializedName("followed_by")
        public final boolean followedBy;

        public Party(long id, String idStr, String screenName, boolean following, boolean followedBy) {
            this.id = id;
            this.idStr = idStr;
            this.screenName = screenName;
            this.following = following;
            this.followedBy = followedBy;
        }
    }

    public static class Source extends Party {


        public Source(long id, String idStr, String screenName, boolean following, boolean followedBy) {
            super(id, idStr, screenName, following, followedBy);
        }

    }

    public static class Target extends Party {

        @SerializedName("can_dm")
        public final boolean canDm;

        @SerializedName("blocking")
        public final Boolean blocking;

        @SerializedName("muting")
        public final Boolean muting;

        @SerializedName("all_replies")
        public final Boolean allReplies;

        @SerializedName("want_retweets")
        public final Boolean wantRetweets;

        @SerializedName("marked_spam")
        public final Boolean markedSpam;

        @SerializedName("notifications_enabled")
        public final Boolean notificationsEnabled;

        public Target(long id, String idStr, String screenName, boolean following, boolean followedBy, boolean canDm, Boolean blocking, Boolean muting, Boolean allReplies, Boolean wantRetweets, Boolean markedSpam, Boolean notificationsEnabled) {
            super(id, idStr, screenName, following, followedBy);
            this.canDm = canDm;
            this.blocking = blocking;
            this.muting = muting;
            this.allReplies = allReplies;
            this.wantRetweets = wantRetweets;
            this.markedSpam = markedSpam;
            this.notificationsEnabled = notificationsEnabled;
        }
    }
}
