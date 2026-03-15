package com.xiuxin.app.model;

import java.io.Serializable;

public class User implements Serializable {
    public String id;
    public String name;
    public String email;
    public String avatar;
    public String provider; // "google", "wechat", "email"
    public String token;
}
