package com.pasenture.image;

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

   public List<FileInfo> findByCreatedDate (String createdDate);

}