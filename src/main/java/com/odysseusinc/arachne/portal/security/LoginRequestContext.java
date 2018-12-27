package com.odysseusinc.arachne.portal.security;

public class LoginRequestContext {

    private static ThreadLocal<String> userName = new ThreadLocal<>();

    public static String getUserName() {

        return userName.get();
    }

    public static void setUserName(String userName) {

        LoginRequestContext.userName.set(userName);
    }

    public static void clear() {

        userName.set(null);
    }
}
