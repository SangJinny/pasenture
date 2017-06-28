package com.pasenture.image;


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.*;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
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
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;



    /*    @Autowired
        private AmazonS3Client amazonS3Client;*/

    // 업로드된 파일배열을 각각처리함
    // 1. MultipartFile -> File 변환
    // 2. 메타데이터 처리 후 RDS에 업로드
    // 3. 파일을 S3에 업로드
    public void upload(MultipartFile[] multipartFiles) throws IOException, ImageProcessingException, ParseException {
        for(MultipartFile multipartFile : multipartFiles) {

            if(multipartFile != null &&
                    !StringUtils.isEmpty(multipartFile.getOriginalFilename()) &&
                    multipartFile.getContentType().startsWith("image")
                    ) {

                File file = commonFunction.getFileFromMultipartFile(multipartFile);
                FileInfo tempFileInfo = uploadOnRDS(file);
                File thumbnailFile = commonFunction.getThumbnailFromFile(file);
                System.out.println("########################"+thumbnailFile.getName());
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
                System.out.println("FileName: "+fileName+" / Progress: "+progress+"%");
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
    private FileInfo uploadOnRDS (File file) throws IOException, ImageProcessingException, ParseException {

        FileInfo fileInfo = extractFileInfo(file);
        System.out.println("INSERT!!: "+fileInfo.toString());
        fileInfoRepository.save(fileInfo);

        return fileInfo;
    }

    public List<FileInfo> searchByDate (String dateString) throws ParseException {
        //return fileInfoRepository.findAll();

        List<FileInfo> fileInfoList = fileInfoRepository.findByCreatedDate(dateString);
        //System.out.println(fileInfoList.get(0).toString());
        return fileInfoList;
    }

    private FileInfo extractFileInfo (File file) throws ImageProcessingException, IOException, ParseException {

        Metadata metadata = ImageMetadataReader.readMetadata(file);
        FileInfo fileinfo = new FileInfo(file.getName());

        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getDirectory(ExifSubIFDDirectory.class);
        Date originalDate = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        String modelName = exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_LENS_MODEL);

        fileinfo.setOriginalDate(originalDate);
        fileinfo.setModelName(modelName);
        if(originalDate != null) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd-HH:mm:ss");
            String createdDate = dateFormat.format(originalDate).substring(0,10);
            String createdTime = dateFormat.format(originalDate).substring(11);
            String createdDay = commonFunction.getDateDay(createdDate, "YYYY-MM-dd");

            fileinfo.setCreatedDate(createdDate);
            fileinfo.setCreatedTime(createdTime);
            fileinfo.setCreatedDay(createdDay);
        }

        GpsDirectory gpsDirectory = metadata.getDirectory(GpsDirectory.class);
        if(gpsDirectory != null) {

            GeoLocation geoLocation = gpsDirectory.getGeoLocation();
            if(geoLocation != null && !geoLocation.isZero()) {

                fileinfo.setLatitude(geoLocation.getLatitude());
                fileinfo.setLatitude(geoLocation.getLongitude());
            }
        }

        return fileinfo;
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /*
    private PutObjectResult uploadOnS3(String filePath, String uploadKey) throws FileNotFoundException {
        return uploadOnS3(new FileInputStream(filePath), uploadKey);
    }

    private PutObjectResult uploadOnS3(InputStream inputStream, String uploadKey) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, uploadKey, inputStream, new ObjectMetadata());
        PutObjectResult putObjectResult = amazonS3Client.putObject(putObjectRequest);
        IOUtils.closeQuietly(inputStream);
        return putObjectResult;
    }

    private S3ObjectInputStream getFileOnStream (String key) {

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
        S3Object s3Object = amazonS3Client.getObject(getObjectRequest);
        S3ObjectInputStream objectInputStream = s3Object.getObjectContent();
        return objectInputStream;
    }

    public void inquire (String key, HttpServletResponse response) throws IOException {

        S3ObjectInputStream objectInputStream = getFileOnStream(key);
        IOUtils.copy(objectInputStream, response.getOutputStream());
    }

    public ResponseEntity<byte[]> download(String key) throws IOException {
        S3ObjectInputStream objectInputStream = getFileOnStream(key);
        byte[] bytes = IOUtils.toByteArray(objectInputStream);
        String fileName = URLEncoder.encode(key, "UTF-8").replaceAll("\\+", "%20");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentLength(bytes.length);
        httpHeaders.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }



    public List<S3ObjectSummary> list() {
        ObjectListing objectListing = amazonS3Client.listObjects(new ListObjectsRequest().withBucketName(bucket));
        List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
        return s3ObjectSummaries;
    }
    */
}
