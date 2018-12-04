package com.example.kwonyoung_jae.fashionistagram;

/**
 * Created by Kwon Young-jae on 2018-11-24.
 */

public class AlarmDTO {
    public String destinationUid;

    public String userId;

    public String uid;

    public int kind; //0 : 좋아요, 1: 팔로우, 2: 메세지

    public long timestamp;
    public String message;
}
