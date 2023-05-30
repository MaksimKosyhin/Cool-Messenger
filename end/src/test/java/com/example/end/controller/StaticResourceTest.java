package com.example.end.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StaticResourceTest {
    //todo:rewrite this
    //todo:find out why @Value not working
    private static final String uploadsFolder = "uploads-test";

    private static final String profileImages = "images";

    private static final String attachments = "attachments";

    private final String profileImagesPath = uploadsFolder + "/" + profileImages;

    private final String attachmentsPath = uploadsFolder + "/" + attachments;

    private final MockMvc mockMvc;

    @Autowired
    public StaticResourceTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void checkStaticFolder_whenAppIsInitialized_rootFolderMustExist() {
        File uploadFolder = new File(uploadsFolder);
        boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();
        assertThat(uploadFolderExist).isTrue();
    }

    @Test
    public void checkStaticFolder_whenAppIsInitialized_ProfilePhotosSubFolderMustExist() {
        String profileImageFolderPath = profileImagesPath;
        File profileImageFolder = new File(profileImageFolderPath);
        boolean profileImageFolderExist = profileImageFolder.exists() && profileImageFolder.isDirectory();
        assertThat(profileImageFolderExist).isTrue();
    }

    @Test
    public void checkStaticFolder_whenAppIsInitialized_attachmentsSubFolderMustExist() {
        String attachmentsFolderPath = attachmentsPath;
        File attachmentsFolder = new File(attachmentsFolderPath);
        boolean attachmentsFolderExist = attachmentsFolder.exists() && attachmentsFolder.isDirectory();
        assertThat(attachmentsFolderExist).isTrue();
    }

    @Test
    public void getStaticFile_whenImageExistInProfileUploadFolder_receiveOk() throws Exception {
        String fileName = "profile.png";
        File source = new ClassPathResource(fileName).getFile();

        File target = new File(profileImagesPath + "/" + fileName);
        FileUtils.copyFile(source, target);

        mockMvc.perform(get("/file/" + profileImages+"/"+fileName)).andExpect(status().isOk());
    }

    @Test
    public void getStaticFile_whenImageExistInAttachmentFolder_receiveOk() throws Exception {
        String fileName = "profile-picture.png";
        File source = new ClassPathResource("profile.png").getFile();

        File target = new File(attachmentsPath + "/" + fileName);
        FileUtils.copyFile(source, target);

        mockMvc.perform(get("/file/" + attachments + "/"+fileName)).andExpect(status().isOk());

    }

    @Test
    public void getStaticFile_whenImageDoesNotExist_receiveNotFound() throws Exception {
        mockMvc.perform(get("/file/" + profileImages +"/there-is-no-such-image.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getStaticFile_whenImageExistInAttachmentFolder_receiveOkWithCacheHeaders() throws Exception {
        String fileName = "profile-picture.png";
        File source = new ClassPathResource("profile.png").getFile();

        File target = new File(attachmentsPath + "/" + fileName);
        FileUtils.copyFile(source, target);

        MvcResult result = mockMvc.perform(get("/file/" + attachments +"/"+fileName)).andReturn();

        String cacheControl = result.getResponse().getHeaderValue("Cache-Control").toString();
        assertThat(cacheControl).containsIgnoringCase("max-age=31536000");
    }

    @AfterEach
    public void cleanup() throws IOException {
        FileUtils.cleanDirectory(new File(uploadsFolder + "/" + profileImages));
        FileUtils.cleanDirectory(new File(uploadsFolder + "/" + attachments));
    }

    @BeforeAll
    public static void createTestFolders() {
        createNonExistingFolder(uploadsFolder + "/" + profileImages);
        createNonExistingFolder(uploadsFolder + "/" + attachments);
    }

    private static void createNonExistingFolder(String path) {
        File folder = new File(path);
        boolean folderExist = folder.exists() && folder.isDirectory();
        if(!folderExist) {
            folder.mkdir();
        }
    }
}
