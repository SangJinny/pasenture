package com.pasenture.map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Jeon on 2017-07-13.
 */
public interface MapApiManager {

    public ResponseEntity<String> getGpsToAddrJsonResult(double xPos, double yPos);
    public String getRoadAddrInfoFromJson(String Json);
    public String getParcelAddrInfoFromJson(String Json);
}
