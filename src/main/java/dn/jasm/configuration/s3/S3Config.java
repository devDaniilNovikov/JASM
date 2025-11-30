//package dn.jasm.configuration.s3;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.presigner.S3Presigner;
//
//@Configuration
//public class S3Config {
//
//
//    @Value("${s3.region}")
//    private String region;
//
//    @Value("${s3.accessKey}")
//    private String accessKey;
//
//    @Value("${s3.secretKey}")
//    private String secretKey;
//
//    @Bean
//    public S3Presigner s3Client(){
//        S3Presigner.Builder s3 = S3Presigner.builder()
//                .region(Region.of(region));
//        return s3.build();
//    }
//}
