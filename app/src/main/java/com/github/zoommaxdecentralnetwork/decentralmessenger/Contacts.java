package com.github.zoommaxdecentralnetwork.decentralmessenger;

import java.util.HashMap;
import java.util.List;

public class Contacts {
    String key;
    String name;
    int newMsg;

    public Contacts(String key, String name, String newMsg){
        this.key = key;
        this.name = name;
        this.newMsg = Integer.parseInt(newMsg);
    }
}
