package com.aureport.ultra.core.config;

import com.aureport.ultra.core.provider.image.DefaultImageProvider;
import com.aureport.ultra.core.provider.image.HttpImageProvider;
import com.aureport.ultra.core.provider.image.HttpsImageProvider;
import com.aureport.ultra.core.provider.report.classpath.ClasspathReportProvider;
import com.aureport.ultra.core.provider.report.file.FileReportProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfiguration {

    @Bean
    public DefaultImageProvider defaultImageProvider() {
        return new DefaultImageProvider();
    }

    @Bean
    public FileReportProvider fileReportProvider(@Value("${aureport-ultra.fileStoreDir:/WEB-INF/ureportfiles}") String fileStoreDir,
                                                 @Value("${aureport-ultra.disableFileProvider:false}") boolean disabled) {
        FileReportProvider provider = new FileReportProvider();
        provider.setFileStoreDir(fileStoreDir);
        provider.setDisabled(disabled);
        return provider;
    }

    @Bean
    public HttpImageProvider httpImageProvider() {
        return new HttpImageProvider();
    }

    @Bean
    public HttpsImageProvider httpsImageProvider() {
        return new HttpsImageProvider();
    }

    @Bean
    public ClasspathReportProvider classpathReportProvider() {
        return new ClasspathReportProvider();
    }
}
