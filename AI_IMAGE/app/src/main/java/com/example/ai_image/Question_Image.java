package com.example.ai_image;



import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Question_Image extends AppCompatActivity {
    private ImageView btn_img_src;
    private Button clear_img_txt;
    private Bitmap selectedBitmap;
    private ProgressBar progressBar;
    private EditText input_txt;
    private TextView textViewResponse;
    private ImageView imageView;
    private static final String API_URL = "http://192.168.0.3:8080/question_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question_image);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        btn_img_src=findViewById(R.id.btn_src_img);
        imageView = findViewById(R.id.imageGambar);
        clear_img_txt = findViewById(R.id.clear_image);
        input_txt = findViewById(R.id.input_question);
        progressBar = findViewById(R.id.progressBar);  // Initialize ProgressBar
        textViewResponse = findViewById(R.id.textViewResponse);

        Button buttonSendToApi = findViewById(R.id.analys_image);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            btn_img_src.setImageDrawable(null);
                            selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            imageView.setImageBitmap(selectedBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        //select image
        btn_img_src.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            }
        });

        clear_img_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageDrawable(null);
                input_txt.setText("");
                btn_img_src.setImageResource(R.drawable.camera);
                textViewResponse.setText("");
            }
        });


        // Send to API Button
        buttonSendToApi.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                String base64String = convertBitmapToBase64(selectedBitmap);
                //Toast.makeText(this, base64String, Toast.LENGTH_LONG).show();
                sendImageToApi(base64String);
                //textViewResponse.setText(base64String);
            } else {
                Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            }
        });



    }


    // Method to convert Bitmap to Base64
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Method to send image to API
    private void sendImageToApi(String base64String) {
        runOnUiThread(() -> textViewResponse.setText(""));
        // Show ProgressBar while the request is in progress
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

        new Thread(() -> {
            try {
                // Create JSON object
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("image", base64String);
                jsonObject.put("question", input_txt.getText());

                // Create Request Body
                MediaType JSON = MediaType.get("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(0, TimeUnit.MILLISECONDS)   // No connection timeout
                        .readTimeout(0, TimeUnit.MILLISECONDS)      // No read timeout
                        .writeTimeout(0, TimeUnit.MILLISECONDS)     // No write timeout
                        .build();

                // Create HTTP Request

                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .build();

                // Execute Request
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        runOnUiThread(() -> {
                            try {
                                // Parse the JSON response
                                JSONObject responseJson = new JSONObject(responseBody);
                                String result = responseJson.optString("result", "No result found");
                                textViewResponse.setText(result);
                            } catch (Exception e) {
                                textViewResponse.setText("Error parsing response");
                            }
                        });
                    } else {
                        runOnUiThread(() -> textViewResponse.setText("Failed to upload image: " + response.message()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> textViewResponse.setText("Error: " + e.getMessage()));
            } finally {
                // Hide ProgressBar after the request is completed
               runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        }).start();
    }




}
