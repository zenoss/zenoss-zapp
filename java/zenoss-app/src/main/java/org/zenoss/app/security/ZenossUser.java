package org.zenoss.app.security;

import com.google.common.base.Preconditions;

/**
 * Primary zenoss user.
 *
 * Created with IntelliJ IDEA.
 * User: scleveland
 * Date: 4/8/14
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ZenossUser {

    public static final String USERNAME_HTTP_HEADER = "X-ZAuth-UserName";
    public static final String PASSWORD_HTTP_HEADER = "X-ZAuth-Password";

    public ZenossUser(String username, String password) {
        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        this.username = username;
        this.password = password;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZenossUser that = (ZenossUser) o;

        if (!password.equals(that.password)) return false;
        if (!username.equals(that.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ZenossUser{" +
                "username='" + username + '\'' +
                ", password='*********'" +
                '}';
    }

    private final String username;
    private final String password;
}
