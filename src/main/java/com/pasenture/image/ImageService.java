package com.pasenture.image;


import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.pasenture.error.PasentureException;
import com.pasenture.map.MapService;
import com.pasenture.module.CommonFunction;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jeon on 2017-05-07.
 */
@Service
public class ImageService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private CommonFunction commonFunction;

    @Autowired
    private MapService mapService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

/*    @Autowired
    private MessageSource messageSource;*/

    /*    @Autowired
        private AmazonS3Client amazonS3Client;*/

    // 업로드된 파일배열을 각각처리함
    // 1. MultipartFile -> File 변환
    // 2. 메타데이터 처리 후 RDS에 업로드
    // 3. 파일을 S3에 업로드
    public void upload(MultipartFile[] multipartFiles) throws PasentureException {

        // 업로드된 파일의 형식을 검증한다.
        // 추후에 배열로 입력을 받지 않도록 개선하도록 하자.
        for(MultipartFile file : multipartFiles) {

            if(!file.getContentType().equals("image/jpeg") &&
                    !file.getContentType().equals("image/jpg")) {

                //throw new PasentureException(messageSource.getMessage("error.file.type.wrong", null, Locale.getDefault()));
                throw new PasentureException("JPG 또는 JPEG파일만 업로드 가능합니다.");
            }
        }


        for(MultipartFile multipartFile : multipartFiles) {

            if(multipartFile != null &&
                    !StringUtils.isEmpty(multipartFile.getOriginalFilename()) &&
                    multipartFile.getContentType().startsWith("image")
                    ) {

                File file = commonFunction.getFileFromMultipartFile(multipartFile);
                FileInfo tempFileInfo = uploadOnRDS(file);
                File thumbnailFile = commonFunction.getThumbnailFromFile(file, 300);
                uploadOnS3(tempFileInfo.getFileKey(), file);
                uploadOnS3(tempFileInfo.getThumbnailKey(),thumbnailFile);
            }
        }
    }



    //S3에 파일을 업로드한다.
    public void uploadOnS3(String fileName, File file) throws PasentureException {

        TransferManager transferManager = new TransferManager(this.amazonS3);
        PutObjectRequest request = new PutObjectRequest(bucket, fileName, file);

        request.setGeneralProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {

                double progress = progressEvent.getBytes() != 0 ?
                        (double)progressEvent.getBytesTransferred() / (double)progressEvent.getBytes()*(double)100 : 0.0;
                //System.out.println("FileName: "+fileName+" / Progress: "+progress+"%");
            }
        });

        Upload upload = transferManager.upload(request);
        try {
            // You can block and wait for the upload to finish
            upload.waitForCompletion();
        } catch (AmazonClientException amazonClientException) {
            amazonClientException.printStackTrace();
            throw new PasentureException("이미지 서버 접속 도중 오류가 발생했습니다.");
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new PasentureException("이미지 서버 접속 도중 오류가 발생했습니다.");
        }
    }

    // S3에서 파일이 들어있는 InputStream 가져옴
    public InputStream getStreamOnS3(String fileName) throws PasentureException {

        try {
            InputStream inputStream = null;
            Resource resource = this.resourceLoader.getResource("s3://imagestorage-seoul-001/"+fileName);
            if(!resource.exists()) {
                throw new PasentureException("이미지 서버에 해당 파일이 없습니다.");
            }

            return resource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new PasentureException("이미지 서버 접속 도중 오류가 발생했습니다.");
        }


    }

    // InputStream에서 파일을 byte형태로 가져옴 (다운로드)
    public ResponseEntity<byte[]> downloadFile (String fileName) throws PasentureException {
        InputStream inputStream = null;
        byte[] bytes = null;
        String downloadFileName = "";
        try {
            inputStream = getStreamOnS3(fileName);
            bytes = IOUtils.toByteArray(inputStream);
            downloadFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

            File tempFile = new File(fileName);

            if(!downloadFileName.endsWith(".jpg") && !downloadFileName.endsWith(".JPG")) {

                downloadFileName += ".jpg";
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PasentureException("파일 다운로드 도중 오류가 발생했습니다.");
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);
        httpHeaders.setContentLength(bytes.length);
        //httpHeaders.setContentDispositionFormData("file", downloadFileName);
        httpHeaders.set("Content-Disposition", "attachment; filename="+downloadFileName);
        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }

    // InputStream의 파일을 HttpResponse에 넣음 (조회)
    public void inquireFile (String fileName, HttpServletResponse response) throws PasentureException {

        InputStream inputStream = null;
        try {
            inputStream = getStreamOnS3(fileName);
            IOUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new PasentureException("파일 다운로드 도중 오류가 발생했습니다.");
        }
    }

/*    public void inquireThumbnailImage (String fileName, HttpServletResponse response) throws IOException {

        //URL에서 이미지 파일을 읽음
        URL url = new URL("http://localhost:8080/uploadedImage/"+fileName);
        BufferedImage originalImage = ImageIO.read(url);

        // 썸네일로 변경하여 OutputStream에 적재
        Thumbnails.of(originalImage)
                .scale(0.25)
                .outputFormat("jpg")
                .toOutputStream(response.getOutputStream());
    }*/

    // RDS에 파일 메타정보를 INSERT
    private FileInfo uploadOnRDS (File file) throws PasentureException {

        FileInfo fileInfo = extractFileInfo(file);
        fileInfoRepository.save(fileInfo);

        return fileInfo;
    }

    public List<FileInfo> searchByDate (String targetDate, String divCode) throws ParseException {

        List<FileInfo> fileInfoList = Collections.emptyList();
        switch (divCode) {

            // 찍은날짜기반
            case "1":
                fileInfoList = fileInfoRepository.findByCreatedDateOrderByCreatedDateAsc(targetDate);
                break;

            // 업데이트날짜기반
            case "2":
                fileInfoList = fileInfoRepository.findByUploadedDateOrderByCreatedDateAsc(targetDate);
                break;
        }
        return fileInfoList;
    }

    public List<FileInfo> searchBetweenDates (String startDate, String endDate, String divCode) {

        List<FileInfo> fileInfoList = Collections.emptyList();
        switch (divCode) {

            // 찍은날짜기반
            case "1":
                fileInfoList = fileInfoRepository.findByCreatedDateBetweenOrderByCreatedDateAsc(startDate, endDate);
                break;

            // 업데이트날짜기반
            case "2":
                fileInfoList = fileInfoRepository.findByUploadedDateBetweenOrderByCreatedDateAsc(startDate, endDate);
                break;
        }
        return fileInfoList;
    }

    public List<FileInfo> searchLikeAddress (String address) {

        List<FileInfo> fileInfoList = Collections.emptyList();
        fileInfoList = fileInfoRepository.
                findByRoadAddressContainingOrParcelAddressContainingOrderByCreatedDateAsc(address, address);
        return fileInfoList;
    }

    public FileInfo selectOne (String key) {

        return fileInfoRepository.getOne(key);
    }

    private FileInfo extractFileInfo (File file) throws PasentureException {

        FileInfo fileinfo = new FileInfo(file.getName());

        try {

            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory exifSubIFDDirectory = metadata.getDirectory(ExifSubIFDDirectory.class);
            Date originalDate = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            String modelName = exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL);

            if(originalDate != null) {

                fileinfo.setOriginalDate(originalDate);
                SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd-HH:mm:ss");
                String createdDate = dateFormat.format(originalDate).substring(0,10);
                String createdTime = dateFormat.format(originalDate).substring(11);
                String createdDay = commonFunction.getDateDay(createdDate, "yyyy-MM-dd");

                fileinfo.setCreatedDate(createdDate);
                fileinfo.setCreatedTime(createdTime);
                fileinfo.setCreatedDay(createdDay);
            } else {

                fileinfo.setCreatedDate("날짜정보없음");
                fileinfo.setCreatedTime("시간정보없음");
                fileinfo.setCreatedDay("");
            }
            if(StringUtils.isEmpty(modelName)) {

                fileinfo.setModelName("카메라정보없음");
            } else {

                fileinfo.setModelName(modelName);
            }

            GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
            if(gpsDirectory != null) {

                GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                if(geoLocation != null && !geoLocation.isZero()) {

                    fileinfo.setLatitude(geoLocation.getLatitude());
                    fileinfo.setLongitude(geoLocation.getLongitude());
                    fileinfo.setRoadAddress(mapService.getRoadAddrFromGPS(geoLocation.getLongitude(),geoLocation.getLatitude()));
                    fileinfo.setParcelAddress(mapService.getParcelAddrFromGPS(geoLocation.getLongitude(),geoLocation.getLatitude()));
                }
            } else {

                fileinfo.setRoadAddress("장소정보없음");
                fileinfo.setParcelAddress("장소정보없음");
            }

            fileinfo.setUploadedDate(commonFunction.getTodayString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new PasentureException("이미지 메타정보 추출 도중 오류가 발생했습니다.");
        }

        return fileinfo;
    }



}
