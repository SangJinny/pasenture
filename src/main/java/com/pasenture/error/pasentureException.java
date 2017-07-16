package com.pasenture.error;

/**
 * Created by Jeon on 2017-07-16.
 */
public class pasentureException extends Exception {

    public pasentureException(String message) {

        super(message);
    }
    public pasentureException(String message, String[] keywords) {

        super(message);
    }
}
