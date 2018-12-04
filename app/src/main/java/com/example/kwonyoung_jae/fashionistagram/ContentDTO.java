package com.example.kwonyoung_jae.fashionistagram;

import java.util.HashMap;
import java.util.Map;

public class ContentDTO {
    public String explain;
    public String imageUrl = null;

    public String uid;

    public String userId;
    public String photoid;
    public long timestamp;
    public String feature;

    public int favoriteCount = 0;

    public Map<String, Boolean> favorites = new HashMap<>();


    public static class Comment {
        public String photoid;
        public String uid;
        public String userId;
        public String comment;
        public long timestamp;
    }
    @Override
    public String toString(){
        return "uid = " +uid +", userid ="+userId;
    }
}

