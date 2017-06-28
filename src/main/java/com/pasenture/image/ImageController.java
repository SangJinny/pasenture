package com.pasenture.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jeon on 2017-05-08.
 */
@Controller
public class ImageController {


    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String uploadPage() {

        return "uploadPage";
    }

    @RequestMapping(value="/search", method = RequestMethod.GET)
    public String searchPage() throws IOException {

        return "searchPage";
    }

    @RequestMapping(value = "/uploadedImage/{key}", method = RequestMethod.GET)
    public void inquireImage(HttpServletResponse response, @PathVariable String key) throws IOException {

        imageService.inquireFile(key, response);
    }
}