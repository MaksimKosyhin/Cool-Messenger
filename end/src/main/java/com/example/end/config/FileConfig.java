package com.example.end.config;

import com.example.end.config.properties.DirectoryPaths;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class FileConfig implements WebMvcConfigurer {

    private final DirectoryPaths props;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + props.getUploadPath() + "/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }

    @Bean
    CommandLineRunner createUploadFolder() {
        return (args) -> {
            createNonExistingFolder(props.getUploadPath());
            createNonExistingFolder(props.getFullProfileImagesPath());
            createNonExistingFolder(props.getFullAttachmentsPath());
        };
    }

    @PreDestroy
    public void cleanDirectories() throws IOException {
        FileUtils.cleanDirectory(new File(props.getFullProfileImagesPath()));
        FileUtils.cleanDirectory(new File(props.getFullAttachmentsPath()));
    }

    private void createNonExistingFolder(String path) {
        File folder = new File(path);
        boolean folderExist = folder.exists() && folder.isDirectory();
        if(!folderExist) {
            folder.mkdir();
        }
    }
}
