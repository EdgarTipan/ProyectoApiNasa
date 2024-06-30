package ec.edu.uce.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import ec.edu.uce.model.Api;
import ec.edu.uce.model.MarsPhoto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class NasaApi implements Api {

    private static final String API_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=1000&api_key=DEMO_KEY";
    private OkHttpClient client;
    private Gson gson;

    public NasaApi() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    @Override
    public List<MarsPhoto> fetchPhotos() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Error inesperado: " + response);

            String jsonData = response.body().string();
            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
            JsonArray photosArray = jsonObject.getAsJsonArray("photos");

            Type photoListType = new TypeToken<List<MarsPhoto>>(){}.getType();
            List<MarsPhoto> marsPhotos = gson.fromJson(photosArray, photoListType);


            return marsPhotos.stream()
                    .peek(marsPhoto -> marsPhoto.setImg_src(marsPhoto.getImg_src().replace("http://", "https://")))
                    .collect(Collectors.toList());
        }
    }
}
