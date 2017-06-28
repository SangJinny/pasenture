package com.pasenture.module;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Jeon on 2017-06-06.
 */
@Service
public class CommonFunction {
    /**
     *
     * @param date
     * @param dateType
     * @return String
     * @throws ParseException
     */
    public String getDateDay(String date, String dateType) throws ParseException {

        String day = "" ;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType) ;
        Date nDate = dateFormat.parse(date) ;
        Calendar cal = Calendar.getInstance() ;
        cal.setTime(nDate);

        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;
        switch(dayNum){
            case 1:
                day = "SUN";
                break ;
            case 2:
                day = "MON";
                break ;
            case 3:
                day = "THU";
                break ;
            case 4:
                day = "WED";
                break ;
            case 5:
                day = "THR";
                break ;
            case 6:
                day = "FRI";
                break ;
            case 7:
                day = "SAT";
                break ;

        }
        return day ;
    }

    /** MultipartFile to File
     *
     * @param multipartFile
     * @return File
     * @throws IOException
     */
    public File getFileFromMultipartFile (MultipartFile multipartFile) throws IOException {

        File file = new File(multipartFile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();

        return file;
    }

    public File getThumbnailFromFile (File file) throws IOException {

        File thumbnail = new File(file.getName()+"_thumbnail");
        thumbnail.createNewFile();
        FileOutputStream fos = new FileOutputStream(thumbnail);
        Thumbnails.of(file).scale(0.25).outputFormat("jpg").toOutputStream(fos);
        fos.close();

        return thumbnail;
    }
}