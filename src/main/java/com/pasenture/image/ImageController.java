package com.pasenture.image;

import com.pasenture.error.PasentureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jeon on 2017-05-08.
 */
@Controller
public class ImageController {


    @Autowired
    private ImageService imageService;

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String mainPage() throws IOException {

        return "redirect:search";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String uploadPage() {

        return "uploadPage";
    }

    @RequestMapping(value="/search", method = RequestMethod.GET)
    public ModelAndView searchPage(@RequestParam(required = false) Map<String, Double> params) {

        Map tempParams = new HashMap();

        // 지도에서 클릭해서 호출하지 않았으면, 파라미터가 없으므로...
        if(params.size() == 0) {

            tempParams.put("divCode", "1");
            tempParams.put("maxLat", "");
            tempParams.put("minLat", "");
            tempParams.put("maxLng", "");
            tempParams.put("minLng", "");
            params = tempParams;
        }

        ModelAndView mav = new ModelAndView("searchPage","params", params);
        return mav;
    }

    @RequestMapping(value="/map", method = RequestMethod.GET)
    public String mapPage() {

        return "mapPage";
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
