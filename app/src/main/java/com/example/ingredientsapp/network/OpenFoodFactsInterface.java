package com.example.ingredientsapp.network;

import com.example.ingredientsapp.data.remote.ResponseProduct;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenFoodFactsInterface {
    @GET("cgi/search.pl?json=1&fields=product_name,brands,image_url,ingredients_text_en,lc=en,lang=en")
    Call<ResponseProduct> getSearchProduct(
            @Query("search_terms") String searchTerms,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("api/v0/product/{code}.json")
    Call<ResponseProduct> getProductDetails(
            @Path("code") String code
    );
}
