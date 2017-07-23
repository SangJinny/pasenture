package com.pasenture.module;

import com.pasenture.error.PasentureException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public String getDateDay(String date, String dateType) throws PasentureException {

        String day = "" ;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateType) ;
        Date nDate = null;

        try {
            nDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new PasentureException("요일 계산 도중 오류가 발생했습니다.");
        }

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
    public File getFileFromMultipartFile (MultipartFile multipartFile) throws PasentureException {

        File file = new File(multipartFile.getOriginalFilename());
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new PasentureException("파일 변환 처리 도중 오류가 발생했습니다.");
        }

        return file;
    }

    /**
     * @param file
     * @return thumnail_file
     * @throws IOException
     */
    public File getThumbnailFromFile (File file, int thumnailWidth) throws PasentureException {

        File thumbnail = new File(file.getName()+"_thumbnail");

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            double rate = (double) thumnailWidth/ width;

            int thumnailHeight = (int)(height * rate);
            thumbnail.createNewFile();
            FileOutputStream fos = new FileOutputStream(thumbnail);
            Thumbnails.of(file).size(thumnailWidth, thumnailHeight).outputFormat("jpg").toOutputStream(fos);
            //Thumbnails.of(file).scale(0.15).outputFormat("jpg").toOutputStream(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new PasentureException("이미지 리사이징 도중 오류가 발생했습니다.");
        }

        return thumbnail;
    }

    /**
     * @return 오늘날짜(today, yyyy-MM-dd)
     */
    public String getTodayString () {

        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    }
}
