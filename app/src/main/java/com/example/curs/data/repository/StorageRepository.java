package com.example.curs.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class StorageRepository {
    private static final String TAG = "ImgBBUpload";
    // ВСТАВЬТЕ СЮДА ВАШ API КЛЮЧ С САЙТА IMGBB
    private static final String API_KEY = "8318e6c25e356b1a6f84eb2512c75f0f";

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(String error);
    }

    public void uploadPetPhoto(Context context, Uri imageUri, UploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure("Фото не выбрано");
            return;
        }

        // Выполняем сетевой запрос в фоновом потоке
        new Thread(() -> {
            try {
                // 1. Читаем картинку и переводим в Base64
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] imageBytes = byteBuffer.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // 2. Формируем данные для отправки
                String data = URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(API_KEY, "UTF-8");
                data += "&" + URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(base64Image, "UTF-8");

                // 3. Отправляем POST запрос на ImgBB
                URL url = new URL("https://api.imgbb.com/1/upload");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                // 4. Читаем ответ
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // 5. Парсим JSON и достаем ссылку
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject dataObj = jsonResponse.getJSONObject("data");
                    String imageUrl = dataObj.getString("url");

                    Log.d(TAG, "Успех! Ссылка: " + imageUrl);
                    callback.onSuccess(imageUrl);

                } else {
                    Log.e(TAG, "Ошибка сервера: " + responseCode);
                    callback.onFailure("Ошибка сервера: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки: " + e.getMessage());
                callback.onFailure("Ошибка: " + e.getMessage());
            }
        }).start();
    }
}