package com.suleiman.pagination.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.suleiman.pagination.R;
import com.suleiman.pagination.api.MovieApi;
import com.suleiman.pagination.api.MovieService;
import com.suleiman.pagination.models.ResultMovieDetails;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private int movieId = 0;
    private MovieService movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        movieId = getIntent().getIntExtra(getString(R.string.movie_id), 0);

        movieService = MovieApi.getClient().create(MovieService.class);
        loadMovieDetails();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovieDetailsActivity.this, GroupChatActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadMovieDetails() {
        callMovieDetailsApi().enqueue(new Callback<ResultMovieDetails>() {
            @Override
            public void onResponse(Call<ResultMovieDetails> call, Response<ResultMovieDetails> response) {
                if (response.body() != null) {
                    loadImage(response.body().getPosterPath()).into((ImageView) findViewById(R.id.imgPoster));
                    ((TextView) toolbar.findViewById(R.id.txtTitle)).setText(response.body().getTitle());
                    ((TextView) findViewById(R.id.txtOverview)).setText(response.body().getOverview());
                }
            }

            @Override
            public void onFailure(Call<ResultMovieDetails> call, Throwable t) {

            }
        });
    }

    private Call<ResultMovieDetails> callMovieDetailsApi() {
        return movieService.getMovieDetails(
                "movie/" + movieId + "",
                getString(R.string.my_api_key)
        );
    }

    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w342";

    private DrawableRequestBuilder<String> loadImage(@NonNull String posterPath) {
        return Glide
                .with(this)
                .load(BASE_URL_IMG + posterPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                .centerCrop()
                .crossFade();
    }
}
