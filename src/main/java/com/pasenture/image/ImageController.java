package com.pasenture.image;

import com.pasenture.error.PasentureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Jeon on 2017-05-08.
 */
@Controller
public class ImageController {


    @Autowired
    private ImageService imageService;

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String mainPage() throws IOException {

        return "searchPage";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String uploadPage() {

        return "uploadPage";
    }

    @RequestMapping(value="/search", method = RequestMethod.GET)
    public String searchPage() throws IOException {

        return "searchPage";
    }

    @RequestMapping(value = "/uploadedImage/{key}", method = RequestMethod.GET)
    public void inquireImage(HttpServletResponse response, @PathVariable String key) throws PasentureException {

        imageService.inquireFile(key, response);
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET, produces = "application/json; charset=utf8")
    public ModelAndView detailsPage(@RequestParam String key, HttpServletResponse response) {

        FileInfo fileInfo = imageService.selectOne(key);
        ModelAndView mav =new ModelAndView("detailsPage","FileInfo", fileInfo);
        return mav;
    }

}
