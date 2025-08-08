package com.disp.celesmaproject.util;

import com.disp.celesmaproject.model.UploadResponse;
import com.disp.celesmaproject.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Service
public class YandexDiskService {

    @Value("${yandex.disk.oauth-token}")
    private String oauthToken;

    @Value("${yandex.disk.api.url}")
    private String apiUrl;

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private ObjectMapper objectMapper;

    public UploadResponse uploadFileToYandexDisk(MultipartFile file, User user) throws IOException {
        UploadResponse response = new UploadResponse();
        response.setOriginalFileName(file.getOriginalFilename());

        String newAvatarName = generateFileName(Objects.requireNonNull(file.getOriginalFilename()), user.getUsername());

        // 1. Удалить старый файл.
//        deleteOldAvatar(user.getAvatarName());

        // 2. Параллельное получение URL для загрузки
        String uploadUrl = getUploadUrl(newAvatarName);

        // 3. Загрузка файла
        uploadFile(uploadUrl, file.getBytes(), file.getContentType());

        try{
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 4. Получение превью
        String previewUrl = getFilePreviewUrl(newAvatarName);

        if (previewUrl != null) {
            deleteOldAvatar(user.getAvatarName());
            user.setAvatarName(newAvatarName);
        }

        response.setPreview(previewUrl);
        response.setGeneratedFileName(newAvatarName);

        return response;
    }

    private void deleteOldAvatar(String oldAvatar) throws IOException {
        String encodedPath = URLEncoder.encode(uploadDir, StandardCharsets.UTF_8);

        String url = apiUrl + "/resources?path=" + encodedPath +
                "&fields=items.name&limit=100&preview_size=M";

        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "OAuth " + oauthToken);

        deleteFile(oldAvatar);
    }

    private void deleteFile(String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(uploadDir + "/" + fileName, StandardCharsets.UTF_8);
        String url = apiUrl + "/resources?path=" + encodedFileName + "&permanently=true";

        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete request = new HttpDelete(url);
        request.setHeader("Authorization", "OAuth " + oauthToken);

        client.execute(request);
    }

    private String generateFileName(String originalFileName, String username) {
        // Генерируем UUID
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // Получаем расширение файла
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }

        // Формируем новое имя: avatar_username_uuid.ext
        return String.format("avatar_%s_%s%s",
                username,
                uuid,
                extension);
    }

    private String getUploadUrl(String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(uploadDir + "/" + fileName, StandardCharsets.UTF_8);
        String url = apiUrl + "/resources/upload?path=" + encodedFileName + "&overwrite=true";

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "OAuth " + oauthToken);

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        JsonNode jsonNode = objectMapper.readTree(entity.getContent());
        return jsonNode.get("href").asText();
    }


    private void uploadFile(String uploadUrl, byte[] file, String contentType) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut request = new HttpPut(uploadUrl);

        ByteArrayEntity entity = new ByteArrayEntity(file);
        request.setEntity(entity);
        request.setHeader("Content-Type", contentType);

        // Ожидаем завершения запроса
        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != 201 && statusCode != 200) {
            throw new IOException("Ошибка загрузки файла. Код: " + statusCode);
        }
    }

    private String getFilePreviewUrl(String fileName) throws IOException {
        String encodedFileName = URLEncoder.encode(uploadDir + "/" + fileName, StandardCharsets.UTF_8);

        // 1. Публикуем файл
        String publishUrl = apiUrl + "/resources/publish?path=" + encodedFileName;
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut publishRequest = new HttpPut(publishUrl);
        publishRequest.setHeader("Authorization", "OAuth " + oauthToken);
        client.execute(publishRequest);

        // 2. Получаем информацию о файле
        String infoUrl = apiUrl + "/resources?path=" + encodedFileName + "&preview_size=M";
        HttpGet infoRequest = new HttpGet(infoUrl);
        infoRequest.setHeader("Authorization", "OAuth " + oauthToken);

        HttpResponse infoResponse = client.execute(infoRequest);
        JsonNode infoNode = objectMapper.readTree(infoResponse.getEntity().getContent());

        // 3. Получаем ссылку на превью
        return infoNode.path("preview").asText();
    }
}