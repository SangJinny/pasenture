package com.pasenture.image;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Jeon on 2017-05-20.
 */
@Entity
public class FileInfo {

    @Id
    private String fileKey;

    @Column(nullable = false)
    private String fileType;
    @Column(nullable = false)
    private String fileName;


    private String thumbnailKey;
    private String modelName ="";
    private Date originalDate;
    @Column(length = 10)
    private String createdDate ="";
    @Column(length = 12)
    private String createdTime="";
    @Column(length = 3)
    private String createdDay ="";
    private double latitude =0.0;    // YPos
    private double longitude = 0.0;   // XPos
    private String position = "";
    @Column(length = 500)
    private String memo = "";

    public FileInfo () {
        // default 생성자가 반드시 있어야함.
    }

    public FileInfo (String originalName) {

        this.fileName = originalName;
        this.fileType = originalName.substring(originalName.lastIndexOf(".")+1).toUpperCase();
        String uuid = UUID.randomUUID().toString().replace("-","").substring(0,4);
        this.fileKey = uuid+originalName.substring(0,originalName.lastIndexOf("."));
        this.thumbnailKey = fileKey+"_thumbnail";
    }

    @Override
    public boolean equals(Object obj) {
        FileInfo fileInfo = (FileInfo) obj;
        if(fileInfo.getFileKey().equals(this.fileKey)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {

        return new String("FileInfo[key="+fileKey+", fileType="+fileType+", fileName="
                +fileName+", modelName="+modelName+", originalDate="+originalDate+", latitude(Ypos)="
                +latitude+", longitude(Xpos)="+longitude+", position="+position+"+, memo="+memo+"]");

    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String key) {
        this.fileKey = key;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getThumbnailKey() {
        return thumbnailKey;
    }

    public void setThumbnailKey(String thumbnailKey) {
        this.thumbnailKey = thumbnailKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Date getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(Date originalDate) {
        this.originalDate = originalDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {

        this.createdTime = createdTime;
    }

    public String getCreatedDay() {
        return createdDay;
    }

    public void setCreatedDay(String createdDay) {
        this.createdDay = createdDay;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
