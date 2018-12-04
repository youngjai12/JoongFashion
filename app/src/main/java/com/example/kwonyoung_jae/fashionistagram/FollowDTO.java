package com.example.kwonyoung_jae.fashionistagram;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kwon Young-jae on 2018-11-27.
 */

public class FollowDTO {
    public int followerCount = 0;

    public Map<String, Boolean> followers = new HashMap<>();



    public int followingCount = 0;

    public Map<String, Boolean> followings = new HashMap<>();
}
