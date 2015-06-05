package com.gamesky.card.core;

import com.google.gson.Gson;

/**
 * Created on 6/4/15.
 *
 * @Author lianghongbin
 */
public class ResultGenerator {

    public static String generateError(String message) {
        return generateError(ErrorCode.GENERAL, message);
    }

    public static String generateError(ErrorCode code, String message) {
        Result result = new Result(new Status(code.getCode(), message));
        return toJson(result);
    }

    public static String generate(Object data) {
        return toJson(new Result(data));
    }

    public static String generate(Page page, Object data) {
        return toJson(new Result(page, data));
    }

    private static String toJson(Result result) {
        Gson gson = new Gson();
        return gson.toJson(result);
    }
}