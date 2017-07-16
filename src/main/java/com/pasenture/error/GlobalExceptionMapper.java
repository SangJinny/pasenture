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
    protected ResponseEntity<?> handleIOException(Exception ex) {

        String msg = "파일 업로드/다운로드 중 오류가 발생했습니다.";
        System.out.println("#$#$#####################"+msg);
        return  new ResponseEntity<Exception>(ex,HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
