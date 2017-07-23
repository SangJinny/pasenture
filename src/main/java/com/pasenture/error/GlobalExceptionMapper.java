package com.pasenture.error;

import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

/**
 * Created by Jeon on 2017-06-06.
 */
@ControllerAdvice
public class GlobalExceptionMapper {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    protected ResponseEntity<?> handleException(Exception ex) {

        String msg = ex.getMessage();
        ex.printStackTrace();
        return  new ResponseEntity<String>(msg, HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
