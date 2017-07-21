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
import com.pasenture.map.MapService;
import com.pasenture.module.CommonFunction;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
        private AmazonS3Client amazonS3Client;*/

    // 업로드된 파일배열을 각각처리함
    // 1. MultipartFile -> File 변환
    // 2. 메타데이터 처리 후 RDS에 업로드
    // 3. 파일을 S3에 업로드
    public void upload(MultipartFile[] multipartFiles) throws IOException, ImageProcessingException, ParseException, org.json.simple.parser.ParseException {
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
    public void uploadOnS3(String fileName, File file) {

        TransferManager transferManager = new TransferManager(this.amazonS3);
        PutObjectRequest request = new PutObjectRequest(bucket, fileName, file);

        request.setGeneralProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {

                double progress = progressEvent.getBytes() != 0 ? progressEvent.getBytesTransferred() / progressEvent.getBytes()*100 : 0.0;
                //System.out.println("FileName: "+fileName+" / Progress: "+progress+"%");
            }
        });

        Upload upload = transferManager.upload(request);
        try {
            // You can block and wait for the upload to finish
            upload.waitForCompletion();
        } catch (AmazonClientException amazonClientException) {
            System.out.println("Unable to upload file, upload aborted.");
            amazonClientException.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // S3에서 파일이 들어있는 InputStream 가져옴
    public InputStream getStreamOnS3(String fileName) throws IOException {

        Resource resource = this.resourceLoader.getResource("s3://imagestorage-seoul-001/"+fileName);
        return resource.getInputStream();
    }

    // InputStream에서 파일을 byte형태로 가져옴 (다운로드)
    public ResponseEntity<byte[]> downloadFile (String fileName) throws IOException {
        InputStream inputStream = getStreamOnS3(fileName);
        byte[] bytes = IOUtils.toByteArray(inputStream);
        String downloadFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", downloadFileName);
        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }

    // InputStream의 파일을 HttpResponse에 넣음 (조회)
    public void inquireFile (String fileName, HttpServletResponse response) throws IOException {

        InputStream inputStream = getStreamOnS3(fileName);
        IOUtils.copy(inputStream, response.getOutputStream());
    }

    public void inquireThumbnailImage (String fileName, HttpServletResponse response) throws IOException {

        //URL에서 이미지 파일을 읽음
        URL url = new URL("http://localhost:8080/uploadedImage/"+fileName);
        BufferedImage originalImage = ImageIO.read(url);

        // 썸네일로 변경하여 OutputStream에 적재
        Thumbnails.of(originalImage)
                .scale(0.25)
                .outputFormat("jpg")
                .toOutputStream(response.getOutputStream());
    }

    // RDS에 파일 메타정보를 INSERT
    private FileInfo uploadOnRDS (File file) throws IOException, ImageProcessingException, ParseException, org.json.simple.parser.ParseException {

        FileInfo fileInfo = extractFileInfo(file);
        System.out.println("INSERT!!: "+fileInfo.toString());
        fileInfoRepository.save(fileInfo);

        return fileInfo;
    }

    public List<FileInfo> searchByDate (String targetDate, String divCode) throws ParseException {

        List<FileInfo> fileInfoList = Collections.emptyList();
        switch (divCode) {

            // 찍은날짜기반
            case "1":
                fileInfoList = fileInfoRepository.findByCreatedDate(targetDate);
                break;

            // 업데이트날짜기반
            case "2":
                fileInfoList = fileInfoRepository.findByUploadedDate(targetDate);
                break;
        }
        return fileInfoList;
    }

    public List<FileInfo> searchBetweenDates (String startDate, String endDate, String divCode) {

        List<FileInfo> fileInfoList = Collections.emptyList();
        switch (divCode) {

            // 찍은날짜기반
            case "1":
                fileInfoList = fileInfoRepository.findByCreatedDateBetween(startDate, endDate);
                break;

            // 업데이트날짜기반
            case "2":
                fileInfoList = fileInfoRepository.findByUploadedDateBetween(startDate, endDate);
                break;
        }
        return fileInfoList;
    }

    public List<FileInfo> searchLikeAddress (String address) {

        List<FileInfo> fileInfoList = Collections.emptyList();
        fileInfoList = fileInfoRepository.
                findByRoadAddressContainingOrParcelAddressContaining(address, address);
        return fileInfoList;
    }

    public FileInfo selectOne (String key) {

        return fileInfoRepository.getOne(key);
    }

    private FileInfo extractFileInfo (File file) throws ImageProcessingException, IOException, ParseException, org.json.simple.parser.ParseException {

        Metadata metadata = ImageMetadataReader.readMetadata(file);
        FileInfo fileinfo = new FileInfo(file.getName());

        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getDirectory(ExifSubIFDDirectory.class);
        Date originalDate = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        String modelName = exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL);

        fileinfo.setOriginalDate(originalDate);
        if(originalDate != null) {

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
        }

        fileinfo.setUploadedDate(commonFunction.getTodayString());
        return fileinfo;
    }



}
