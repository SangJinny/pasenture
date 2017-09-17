package com.pasenture.map;

import com.pasenture.error.PasentureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Jeon on 2017-07-13.
 */
public interface MapApiManager {

    public ResponseEntity<String> getGpsToAddrJsonResult(double xPos, double yPos) throws PasentureException;
    public String getRoadAddrInfoFromJson(String Json) throws PasentureException;
    public String getParcelAddrInfoFromJson(String Json) throws PasentureException;
}
