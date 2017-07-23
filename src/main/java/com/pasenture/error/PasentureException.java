package com.pasenture.error;

/**
 * Created by Jeon on 2017-07-16.
 */
public class PasentureException extends Exception {

    public PasentureException(String message) {

        super(message);
    }
    public PasentureException(String message, String[] keywords) {

        // 와일드카드가 있으면.
        if(message.contains("{@}")) {

            String forward = message.substring(0, message.indexOf("{@}"));
            String ending = message.substring(0, message.indexOf("{@}")+1);
            message = forward + keywords + ending;
        }

        new PasentureException(message);
    }
}
