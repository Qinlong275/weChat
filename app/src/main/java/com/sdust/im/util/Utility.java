package com.sdust.im.util;

import com.google.gson.Gson;
import com.sdust.im.bean.NewsList;

public class Utility {
    public static NewsList parseJsonWithGson(final String requestText){
        Gson gson = new Gson();
        return gson.fromJson(requestText, NewsList.class);
    }

}
