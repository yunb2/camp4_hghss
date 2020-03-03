package com.rest.recruit.controller.advice;


import com.rest.recruit.dto.ResultResponse;
import com.rest.recruit.dto.ResultResponseWithoutData;
import com.rest.recruit.dto.SimpleResponse;
import com.rest.recruit.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.text.ParseException;


@ControllerAdvice
public class ExceptionAdvice {

    /**
     * 지원하지 않은 HTTP method 호출 할 경우 발생
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message("지원하지 않는 HTTP method 호출입니다")
                .status("400")
                .success("false").build());
    }

    /**
     *  ClientAbortException - broken pipe
     *
     */
    @ExceptionHandler(ClientAbortException.class)
    protected ResponseEntity clientAbortException(ClientAbortException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .status("500")
                .success("false").build());
    }

    /**
     *  handleException
     *  그 밖에 발생하는 모든 예외 처리, Null Point Exception, 등등
     *  개발자가 직접 핸들링해서 다른 예외로 던지지 않으면 모두 이곳으로 모인다.
     */

    @ExceptionHandler(Exception.class)
    protected ResponseEntity handleException(Exception e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .status("500")
                .success("false").build());
    }
    /**
     *  MissingServletRequestParameterException
     *  parameter가 없을 때
     *
     */

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity handleMissingParams(MissingServletRequestParameterException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message("필요한 값이 잘못되었습니다.")
                .status("400")
                .success("false").build());
    }


    /**
     *  ParseException
     *  ranking API 시 사용되는 Date ParseException
     *
     */
    @ExceptionHandler(ParseException.class)
    public ResponseEntity handleParseException(ParseException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(e.getMessage())
                .status("500")
                .success("false").build());
    }

    @ExceptionHandler(UnValidatedDateTypeException.class)
    public ResponseEntity handleDateType(UnValidatedDateTypeException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(e.getMessage()) //유효하지 않은 date type입니다.
                .status("400")
                .success("false").build());
    }

    @ExceptionHandler(GetCalendarException.class)
    public ResponseEntity handleGetCalendar(GetCalendarException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(e.getMessage())
                .status("500")
                .success("false").build());
    }

    @ExceptionHandler(GetDetailRecruitPageException.class)
    public ResponseEntity handleGetDetailRecruitPage(GetDetailRecruitPageException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(e.getMessage())
                .status("500")
                .success("false").build());

    }

    @ExceptionHandler(RedisToDbException.class)
    public ResponseEntity handleRedisToDB(RedisToDbException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(e.getMessage())
                .status("500")
                .success("false").build());
    }

    /**
     *  unauthorized exception
     *  유효하지않은 토큰인 경우
     *
     */

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity handleUnauthorizedException(UnauthorizedException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message(e.getMessage())
                .status("401")
                .success("false").build());
    }

    /**
     *  Expired Token exception
     *  만료된 토큰인 경우
     *
     */

    @ExceptionHandler(ExpiredTokenException.class)
//    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity handleExpiredTokenException(ExpiredTokenException e) {
        return SimpleResponse.ok(ResultResponse.builder()
                .message(e.getMessage())
                .status("402")
                .success("false").build());
    }

    //token eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhdXRoLkhHSFNTQGdtYWlsLmNvbSIsInVzZXJJZCI6MSwiZW1haWwiOiJhdXRoLkhHSFNTQGdtYWlsLmNvbSIsIm5pY2tuYW1lIjoi6rCA64OY7ZSIIOqwgOqwnOu5hCIsInJvbGUiOjEsInRva2VuVHlwZSI6IkFDQ0VTU19UT0tFTiIsImV4cCI6MTU4MTkzODk5OX0=.PswxsPdt2c4tZCBQotlkVvXtdDOLYQBWmNxwzh8dJhs=

    /**
     *  Expired Jwt exception
     *  jwtException 이용한 global 레벨의 만료된 토큰인 경우
     *
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity handleExpiredJwtException(ExpiredJwtException e) {
        return SimpleResponse.ok(ResultResponseWithoutData.builder()
                .message("만료된 토큰입니다.")
                .status("402")
                .success("false").build());
    }

}
