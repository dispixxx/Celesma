package com.disp.celesmaproject.model;

public class UploadResponse {
    private String originalFileName;
//    private String localFileUrl;    // Локальная ссылка на файл
    private String preview;   // Прямая ссылка на превью в Яндекс.Диске
    private String generatedFileName;

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public void setGeneratedFileName(String newFileName) {
        this.generatedFileName = newFileName;
    }

    public String getGeneratedFileName() {
        return generatedFileName;
    }
}