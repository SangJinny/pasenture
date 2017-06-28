package com.pasenture.config;

import org.springframework.cloud.aws.jdbc.config.annotation.EnableRdsInstance;
import org.springframework.cloud.aws.jdbc.config.annotation.RdsInstanceConfigurer;
import org.springframework.cloud.aws.jdbc.datasource.DataSourceFactory;
import org.springframework.cloud.aws.jdbc.datasource.TomcatJdbcDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Jeon on 2017-05-07.
 * Springboot AWS SDK를 위한 세팅.
 */

@Configuration
@EnableRdsInstance(dbInstanceIdentifier = "pasentureinstance", databaseName = "pasenturedb", username = "sangjinny28", password = "jj4025--", readReplicaSupport = false)
public class AWSConfiguration {

/*    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.region}")
    private String region;

    @Bean
    public BasicAWSCredentials basicAWSCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }

    @Bean
    public AmazonS3Client amazonS3Client(AWSCredentials awsCredentials) {
        AmazonS3Client amazonS3Client = new AmazonS3Client(awsCredentials);
        amazonS3Client.setRegion(Region.getRegion(Regions.fromName(region)));
        return amazonS3Client;
    }*/

    @Bean
    public RdsInstanceConfigurer rdsInstanceConfigurer () {

        return new RdsInstanceConfigurer() {
            @Override
            public DataSourceFactory getDataSourceFactory() {
                TomcatJdbcDataSourceFactory dataSourceFactory = new TomcatJdbcDataSourceFactory();
                dataSourceFactory.setInitialSize(5);
                dataSourceFactory.setValidationQuery("SELECT 1 FROM DUAL");
                return dataSourceFactory;
            }
        };
    }
}