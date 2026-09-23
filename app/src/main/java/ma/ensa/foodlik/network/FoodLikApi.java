package ma.ensa.foodlik.network;

import java.util.List;

import ma.ensa.foodlik.model.CreateOrderRequest;
import ma.ensa.foodlik.model.MenuItem;
import ma.ensa.foodlik.model.Order;
import ma.ensa.foodlik.model.Restaurant;
import ma.ensa.foodlik.model.TrackingInfo;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FoodLikApi {
    @GET("restaurants")
    Call<List<Restaurant>> restaurants();

    @GET("restaurants/{id}")
    Call<Restaurant> restaurant(@Path("id") int id);

    @GET("restaurants/{id}/menu")
    Call<List<MenuItem>> menu(@Path("id") int restaurantId);

    @POST("orders")
    Call<Order> createOrder(@Body CreateOrderRequest request);

    @GET("orders")
    Call<List<Order>> orders(@Query("customerId") String customerId);

    @GET("orders/{id}")
    Call<Order> order(@Path("id") int id);

    @GET("orders/{id}/tracking")
    Call<TrackingInfo> tracking(@Path("id") int id);

    @POST("orders/{id}/status/advance")
    Call<Order> advanceStatus(@Path("id") int id);

    @Multipart
    @POST("orders/{id}/photo")
    Call<PhotoUploadResponse> uploadPhoto(@Path("id") int id, @Part MultipartBody.Part photo);

    class PhotoUploadResponse {
        public int orderId;
        public String photoPath;
    }
}
