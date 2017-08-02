package com.pasenture.image;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by Jeon on 2017-05-27.
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, String>, JpaSpecificationExecutor{

   public List<FileInfo> findByCreatedDateOrderByCreatedDateAsc (String createdDate);

   public List<FileInfo> findByCreatedDate (String createdDate, Pageable pageable);

   public List<FileInfo> findByUploadedDateOrderByCreatedDateAsc (String UploadedDate);

   public List<FileInfo> findByUploadedDate (String UploadedDate, Pageable pageable);

   public List<FileInfo> findByCreatedDateBetweenOrderByCreatedDateAsc (String startDate, String endDate);

   public List<FileInfo> findByCreatedDateBetween (String startDate, String endDate, Pageable pageable);

   public List<FileInfo> findByUploadedDateBetweenOrderByCreatedDateAsc (String startDate, String endDate);

   public List<FileInfo> findByUploadedDateBetween (String startDate, String endDate, Pageable pageable);

   public List<FileInfo> findByRoadAddressContainingOrParcelAddressContainingOrderByCreatedDateAsc (String address1, String address2);

   public List<FileInfo> findByRoadAddressContainingOrParcelAddressContainingOrderByCreatedDateAsc (String address1, String address2, Pageable pageable);
}
