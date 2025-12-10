package com.ctrlf.infra.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * S3 클라이언트/프리사이너 설정.
 * - 자격 증명: DefaultCredentialsProvider (환경변수/프로파일/EC2 Role 등)
 * - 리전: cloud.aws.region (기본 ap-northeast-2)
 */
@Configuration
public class S3Config {

    /**
     * AWS Region 빈.
     *
     * @param region application 설정값
     * @return Region
     */
    @Bean
    public Region awsRegion(@Value("${cloud.aws.region:ap-northeast-2}") String region) {
        return Region.of(region);
    }

    /**
     * S3 동기 클라이언트.
     * Presigner와 동일한 자격 증명/리전을 사용합니다.
     */
    @Bean
    public S3Client s3Client(Region region) {
        return S3Client.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    /**
     * S3 Presigner.
     * Presigned URL 발급에 사용합니다.
     */
    @Bean
    public S3Presigner s3Presigner(Region region) {
        return S3Presigner.builder()
            .region(region)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}

