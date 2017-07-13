package com.pasenture.map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Jeon on 2017-07-13.
 */
@Component
public class NaverMapApiManager implements MapApiManager {

    @Value("${naver.clientId}")
    private String clientId;

    @Value("${naver.clientSecret}")
    private String clientSecret;

    @Override
    public ResponseEntity<String> getGpsToAddrJsonResult(double xPos, double yPos) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Naver-Client-Id", clientId);
        httpHeaders.add("X-Naver-Client-Secret", clientSecret);

        String url = "https://openapi.naver.com/v1/map/reversegeocode?encoding=utf-8"
                +"&query="+xPos+","+yPos;

        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity(httpHeaders), String.class);
    }

    @Override
    public String getAddrInfoFromJson(String Json) {

        String resultAddr = "";

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(Json);
            JSONObject result = (JSONObject) jsonObject.get("result");
            JSONArray addrInfoArray = (JSONArray) result.get("items");

            for(int i = 0 ; i < addrInfoArray.size() ; i++) {

                JSONObject addrInfo = (JSONObject) addrInfoArray.get(i);
                // 도로명 주소만 리턴
                if(addrInfo.get("isRoadAddress").toString().equals("true")) {

                    resultAddr = addrInfo.get("address").toString();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultAddr;
    }
}
