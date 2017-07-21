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
import org.springframework.util.StringUtils;
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
    public String getRoadAddrInfoFromJson(String Json) {

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

    @Override
    public String getParcelAddrInfoFromJson(String Json) {
        String resultAddr = "";

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(Json);
            JSONObject result = (JSONObject) jsonObject.get("result");
            JSONArray addrInfoArray = (JSONArray) result.get("items");

            for(int i = 0 ; i < addrInfoArray.size() ; i++) {

                JSONObject addrInfo = (JSONObject) addrInfoArray.get(i);
                // 도로명 주소가 아니고, 행정구역 지번주소임.
                if(addrInfo.get("isRoadAddress").toString().equals("false") &&
                        addrInfo.get("isAdmAddress").toString().equals("true")) {

                    resultAddr = addrInfo.get("address").toString();
                }
            }

            // 없으면 비행정지번주소로 처리.
            if(StringUtils.isEmpty(resultAddr)) {

                for(int i = 0 ; i < addrInfoArray.size() ; i++) {

                    JSONObject addrInfo = (JSONObject) addrInfoArray.get(i);
                    if(addrInfo.get("isRoadAddress").toString().equals("false") &&
                            addrInfo.get("isAdmAddress").toString().equals("false")) {

                        resultAddr = addrInfo.get("address").toString();
                    }
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultAddr;
    }
}
