package com.ctrlf.chat.strategy;

import java.util.List;

public class PromResponse {

    public Data data;

    public double extractSingleValue() {
        if (data == null || data.result == null || data.result.isEmpty()) {
            return 0.0;
        }
        List<Object> value = data.result.get(0).value;
        return Double.parseDouble(value.get(1).toString());
    }

    public static class Data {
        public List<Result> result;
    }

    public static class Result {
        public List<Object> value;
    }
}
