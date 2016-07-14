package com.example.qkx.speechtotext.model;

import java.util.List;

/**
 * Created by qkx on 16/7/13.
 */
public class ResultBean {
    public String from;
    public String to;
    public List<TransResult> trans_result;

    public class TransResult {
        public String src;
        public String dst;
    }
}
