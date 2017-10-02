package com.suleiman.pagination.api;


import com.suleiman.pagination.models.ResultMovieDetails;
import com.suleiman.pagination.models.TopRatedMovies;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Pagination
 * Created by Suleiman19 on 10/27/16.
 * Copyright (c) 2016. Suleiman Ali Shakir. All rights reserved.
 */

public interface MovieService {

    @GET("movie/now_playing")
    Call<TopRatedMovies> getTopRatedMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int pageIndex
    );

    @GET()
    Call<ResultMovieDetails> getMovieDetails(
            @Url String pageIndex,
            @Query("api_key") String apiKey
    );
}
