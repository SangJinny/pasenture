package com.pasenture.image.api;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.drew.imaging.ImageProcessingException;
import com.pasenture.image.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.pasenture.image.ImageService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Created by Jeon on 2017-05-08.
 */

@RestController
@RequestMapping(value = "/api/image")
public class ImageApiController {

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity upload(MultipartFile[] files) throws ParseException, ImageProcessingException, IOException, org.json.simple.parser.ParseException {

        HttpStatus status = HttpStatus.CREATED;
        imageService.upload(files);
        return new ResponseEntity(status);

    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(@RequestParam String key) throws IOException {
        return imageService.downloadFile(key);
    }

    @RequestMapping(value ="/search/date", method = RequestMethod.GET ,produces = "application/json; charset=utf8")
    public List<FileInfo> search(@RequestParam String date) throws ParseException {

        return imageService.searchByDate(date);
    }
/*    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<S3ObjectSummary> list() throws IOException {
        return imageService.list();
    }*/
}
