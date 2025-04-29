package com.disp.celesmaproject.util;

import com.disp.celesmaproject.model.UploadResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public UploadResponse uploadFileToYandexDisk(MultipartFile file, String username) throws IOException {
        UploadResponse response = new UploadResponse();
        response.setOriginalFileName(file.getOriginalFilename());

        String newFileName = generateFileName(file.getOriginalFilename(), username);

        // 1. Асинхронное удаление старых аватарок
        CompletableFuture.runAsync(() -> {
            try {
                deleteOldAvatars(username);
            } catch (IOException e) {
                // Логирование ошибки
            }
        }, executorService);

        // 2. Параллельное получение URL для загрузки
        String uploadUrl = getUploadUrl(newFileName);

        // 3. Загрузка файла
        uploadFile(uploadUrl, file);

        // 4. Получение превью (можно сделать асинхронно)
        String previewUrl = getFilePreviewUrl(newFileName);

        response.setYandexDiskUrl(previewUrl);
        response.setGeneratedFileName(newFileName);

        return response;
    }

    private void deleteOldAvatars(String username) throws IOException {
        String searchPattern = "avatar_" + username + "_";
        String encodedPath = URLEncoder.encode(uploadDir, StandardCharsets.UTF_8);

        String url = apiUrl + "/resources?path=" + encodedPath +
                "&fields=items.name&limit=100&preview_size=M";

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "OAuth " + oauthToken);

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();

        JsonNode jsonNode = objectMapper.readTree(entity.getContent());
        JsonNode items = jsonNode.path("_embedded").path("items");

        // Удаляем все файлы, начинающиеся с avatar_username_
        for (JsonNode item : items) {
            String fileName = item.path("name").asText();
            if (fileName.startsWith(searchPattern)) {
                deleteFile(fileName);
            }
        }
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

    private void uploadFile(String uploadUrl, MultipartFile file) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut request = new HttpPut(uploadUrl);

        InputStream inputStream = file.getInputStream();
        InputStreamEntity entity = new InputStreamEntity(inputStream, file.getSize());
        request.setEntity(entity);
        request.setHeader("Content-Type", file.getContentType());

        client.execute(request);
        inputStream.close();
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