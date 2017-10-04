package com.tmdb.movieslisting.activity.movieDetails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tmdb.movieslisting.R;
import com.tmdb.movieslisting.activity.chatRoom.ChatRoomActivity;
import com.tmdb.movieslisting.api.MovieApi;
import com.tmdb.movieslisting.api.MovieService;
import com.tmdb.movieslisting.models.Genres;
import com.tmdb.movieslisting.models.ResultMovieDetails;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private int movieId = 0;
    private MovieService movieService;

    private TextView movieVoteCount, movieAverageVote, moviePopularity, movieReleaseDateWithRunTime, movieGenres, movieLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        movieVoteCount = (TextView) findViewById(R.id.movie_vote_count);
        movieAverageVote = (TextView) findViewById(R.id.movie_average_vote);
        moviePopularity = (TextView) findViewById(R.id.movie_popularity);

        movieReleaseDateWithRunTime = (TextView) findViewById(R.id.movie_ReleaseDateWithRunTime);
        movieGenres = (TextView) findViewById(R.id.movie_genres);
        movieLanguage = (TextView) findViewById(R.id.movie_language);

        movieId = getIntent().getIntExtra(getString(R.string.movie_id), 0);

        movieService = MovieApi.getClient().create(MovieService.class);
        loadMovieDetails();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovieDetailsActivity.this, ChatRoomActivity.class);
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
                    ((TextView) findViewById(R.id.movie_overview)).setText(response.body().getOverview());

                    movieVoteCount.setText(response.body().getVoteCount() + " " + getString(R.string.vote_count));
                    movieAverageVote.setText(response.body().getVoteAverage() + "");
                    moviePopularity.setText(response.body().getPopularity() + "");

                    int hours = response.body().getRuntime() / 60;
                    int minutes = response.body().getRuntime() % 60;
                    movieReleaseDateWithRunTime.setText(
                            hours + "h " +
                                    minutes + " min"
                                    + " | " +
                                    response.body().getReleaseDate());
                    movieLanguage.setText(response.body().getOriginalLanguage());

                    String genres = "";
                    for (Genres genre : response.body().getGenres()) {
                        genres = genres + genre.getName() + ", ";
                    }
                    movieGenres.setText(genres.substring(0, genres.length() - 2));
                }
            }

            @Override
            public void onFailure(Call<ResultMovieDetails> call, Throwable t) {
                Snackbar snackbar = Snackbar
                        .make(((CoordinatorLayout) findViewById(R.id.layoutMovieDetails)), t.toString(), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    private Call<ResultMovieDetails> callMovieDetailsApi() {
        return movieService.getMovieDetails(
                "movie/" + movieId + "",
                getString(R.string.my_api_key)
        );
    }

    private String BASE_URL_IMG = "https://image.tmdb.org/t/p/w342";

    private DrawableRequestBuilder<String> loadImage(@NonNull String posterPath) {
        return Glide
                .with(this)
                .load(BASE_URL_IMG + posterPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                .centerCrop()
                .crossFade();
    }
}
