package com.pasenture.map;


import com.pasenture.error.PasentureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by Jeon on 2017-07-09.
 */
@Service
public class MapService {

    /* 메소드 내에서 직접 생성하여 쓰는것이 좋을듯 하다.
    @Autowired
    RestTemplate restTemplate;
    */

    @Value("${naver.clientId}")
    private String clientId;

    @Value("${naver.clientSecret}")
    private String clientSecret;

    @Autowired
    MapApiManager mam;

    public String getRoadAddrFromGPS(double xPos, double yPos) throws PasentureException {

        //MapApiManager mam = new NaverMapApiManager();
        ResponseEntity<String> response = mam.getGpsToAddrJsonResult(xPos, yPos);

        String resultAddr = "";

        // 추후 오류처리가 필요하면 추가하자
        if(response.getStatusCode() == HttpStatus.OK &&
                !StringUtils.isEmpty(response.getBody())) {

            resultAddr = mam.getRoadAddrInfoFromJson(response.getBody());
        }

        if(StringUtils.isEmpty(resultAddr)) {
            resultAddr = "위치정보없음";
        }
        return resultAddr;
    }

    public String getParcelAddrFromGPS(double xPos, double yPos) throws PasentureException {

        //MapApiManager mam = new NaverMapApiManager();
        ResponseEntity<String> response = mam.getGpsToAddrJsonResult(xPos, yPos);

        String resultAddr = "";

        // 추후 오류처리가 필요하면 추가하자
        if(response.getStatusCode() == HttpStatus.OK &&
                !StringUtils.isEmpty(response.getBody())) {

            resultAddr = mam.getParcelAddrInfoFromJson(response.getBody());
        }

        if(StringUtils.isEmpty(resultAddr)) {
            resultAddr = "위치정보없음";
        }
        return resultAddr;
    }
}
