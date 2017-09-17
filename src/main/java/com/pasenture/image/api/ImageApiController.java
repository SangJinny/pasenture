package com.pasenture.image.api;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.drew.imaging.ImageProcessingException;
import com.pasenture.error.PasentureException;
import com.pasenture.image.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.pasenture.image.ImageService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by Jeon on 2017-05-08.
 */

@RestController
@RequestMapping(value = "/api/image")
public class ImageApiController {

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity upload(MultipartFile[] files) throws PasentureException {

        HttpStatus status = HttpStatus.CREATED;
        imageService.upload(files);
        return new ResponseEntity(status);

    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(@RequestParam String key) throws PasentureException {
        return imageService.downloadFile(key);
    }

    @RequestMapping(value ="/search/position", method = RequestMethod.GET, produces = "application/json; charset=utf8")
    public ResponseEntity<Map<String, Object>> search(@RequestParam Map<String, String> params) throws PasentureException {

        Map<String, Object> response = null;

        double minLat = Double.valueOf(params.get("minLat"));
        double maxLat = Double.valueOf(params.get("maxLat"));
        double minLng = Double.valueOf(params.get("minLng"));
        double maxLng = Double.valueOf(params.get("maxLng"));
        String divCode = params.get("divCode");
        int page = Integer.valueOf(params.get("page"));


        response = imageService.searchByMaxMinPosition(minLat, maxLat, minLng, maxLng, page);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @RequestMapping(value ="/search/date", method = RequestMethod.GET ,produces = "application/json; charset=utf8")
    public ResponseEntity<Map<String, Object>> searchByDate(@RequestParam Map<String, String> params/*, @RequestParam String divCode, @RequestParam String startDate,
                                 @RequestParam String endDate, @RequestParam String address, @RequestParam int page*/) throws ParseException, PasentureException {

        Map<String, Object> response = null;

        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        String divCode = params.get("divCode");
        int page = Integer.valueOf(params.get("page"));

        if(StringUtils.isEmpty(endDate)) {

            response = imageService.searchByDate(startDate, divCode, page);
        } else if (StringUtils.isEmpty(startDate)) {

            response = imageService.searchByDate(endDate, divCode, page);
        }  else {

            response = imageService.searchBetweenDates(startDate, endDate, divCode, page);
        }

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/search/address", method = RequestMethod.GET, produces = "application/json; charset=utf8")
    public Map<String, Object>/*List<FileInfo>*/ searchByAddress (@RequestParam String address, @RequestParam int page) throws PasentureException {

        Map<String, Object> response = imageService.searchLikeAddress(address, page);

        return response;
    }

    @RequestMapping(value = "/search/all", method = RequestMethod.GET, produces = "application/json; charset=utf8")
    public Map<String, Object> searchAll () throws PasentureException {

        Map<String, Object> response = imageService.searchAll();

        return response;
    }
}
