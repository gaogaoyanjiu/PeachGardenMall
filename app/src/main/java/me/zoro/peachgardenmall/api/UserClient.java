package me.zoro.peachgardenmall.api;

import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by dengfengdecao on 16/11/11.
 */

public interface UserClient {

    @GET("User/send_validate_code")
    Call<JsonObject> fetchCaptcha(@Query("phone") String tel);

    @POST("User/register")
    Call<JsonObject> saveUser(@Body Map<String, Object> params);

    @POST("User/do_login")
    Call<JsonObject> login(@Body Map<String, String> params);

    // 获取用户详细信息
    @GET("User/get_user_info")
    Call<JsonObject> fetchUserInfo(@Query("userId") int userId);

    // 登出
    @GET("User/logout")
    Call<JsonObject> logout(@Query("userId") int userId);

    // 修改密码
    @POST("User/editPassword")
    Call<JsonObject> changePassword(@Body Map<String, Object> params);

    @POST("User/editPhone")
    Call<JsonObject> changePhone(@Body Map<String, Object> params);

    @POST("User/setIDCard")
    Call<JsonObject> changeIdCard(@Body Map<String, Object> params);


    @GET("user/{username}")
    Call<JsonObject> getUserInfo(@Path("username") String username);

    //修改用户信息
    @POST("User/setUserInfo")
    Call<JsonObject> userInfoRevise(@Body Map<String, Object> params);

    @Multipart
    @POST("User/uploadHeadtop")
    Call<JsonObject> uploadAvatar(@PartMap Map<String, RequestBody> partMap,
                                  @Part MultipartBody.Part file);

    //忘记密码
    @POST("User/reset_password")
    Call<JsonObject> forgetPassword(@Body Map<String, Object> params);


}
