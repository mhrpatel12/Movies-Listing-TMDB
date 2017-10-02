package com.suleiman.pagination.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.suleiman.pagination.R;
import com.suleiman.pagination.models.Result;
import com.suleiman.pagination.utils.PaginationAdapterCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View Types
    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";

    private List<Result> movieResultTopRatedMovies;
    private Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;

    public PaginationAdapter(Context context) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        movieResultTopRatedMovies = new ArrayList<>();
    }

    public List<Result> getMovies() {
        return movieResultTopRatedMovies;
    }

    public void setMovies(List<Result> movieResultTopRatedMovies) {
        this.movieResultTopRatedMovies = movieResultTopRatedMovies;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                View viewItem = inflater.inflate(R.layout.item_list, parent, false);
                viewHolder = new MovieVH(viewItem);
                break;
            case LOADING:
                View viewLoading = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(viewLoading);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Result result = movieResultTopRatedMovies.get(position); // Movie

        switch (getItemViewType(position)) {

            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                movieVH.mMovieTitle.setText(result.getTitle());
                movieVH.mYear.setText(formatYearLabel(result));
                movieVH.mMovieAverageVote.setText(result.getVoteAverage() + "");
                movieVH.mMovieVoteCount.setText(result.getVoteCount() + " " + context.getString(R.string.vote_count));
                movieVH.mMoviePopularity.setText(result.getPopularity() + "");

                // load movie thumbnail
                loadImage(result.getPosterPath())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                // TODO: 08/11/16 handle failure
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // image ready, hide progress now
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;   // return false if you want Glide to handle everything else.
                            }
                        })
                        .into(movieVH.mPosterImg);
                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return movieResultTopRatedMovies == null ? 0 : movieResultTopRatedMovies.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == movieResultTopRatedMovies.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    /*
        Helpers - bind Views
   _________________________________________________________________________________________________
    */

    /**
     * @param result
     * @return [releasedate] | [2letterlangcode]
     */
    private String formatYearLabel(Result result) {
        return result.getReleaseDate()
                + " | "
                + result.getOriginalLanguage().toUpperCase();
    }

    /**
     * Using Glide to handle image loading.
     * Learn more about Glide here:
     *
     * @param posterPath from {@link Result#getPosterPath()}
     * @return Glide builder
     */
    private DrawableRequestBuilder<String> loadImage(@NonNull String posterPath) {
        return Glide
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                .centerCrop()
                .crossFade();
    }


    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    public void add(Result r) {
        movieResultTopRatedMovies.add(r);
        notifyItemInserted(movieResultTopRatedMovies.size() - 1);
    }

    public void addAll(List<Result> moveResultTopRatedMovies) {
        for (Result result : moveResultTopRatedMovies) {
            add(result);
        }
    }

    public void remove(Result r) {
        int position = movieResultTopRatedMovies.indexOf(r);
        if (position > -1) {
            movieResultTopRatedMovies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Result());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = movieResultTopRatedMovies.size() - 1;
        Result result = getItem(position);

        if (result != null) {
            movieResultTopRatedMovies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Result getItem(int position) {
        return movieResultTopRatedMovies.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(movieResultTopRatedMovies.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */
    protected class MovieVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private LinearLayout mLayout;
        private TextView mMovieTitle;
        private TextView mMovieAverageVote;
        private TextView mMovieVoteCount;
        private TextView mMoviePopularity;
        private TextView mYear; // displays "year | language"
        private ImageView mPosterImg;
        private ProgressBar mProgress;

        public MovieVH(View itemView) {
            super(itemView);

            mLayout = (LinearLayout) itemView.findViewById(R.id.layout_movie);
            mMovieTitle = (TextView) itemView.findViewById(R.id.movie_title);
            mMovieAverageVote = (TextView) itemView.findViewById(R.id.movie_average_vote);
            mMovieVoteCount = (TextView) itemView.findViewById(R.id.movie_vote_count);
            mMoviePopularity = (TextView) itemView.findViewById(R.id.movie_popularity);
            mYear = (TextView) itemView.findViewById(R.id.movie_year);
            mPosterImg = (ImageView) itemView.findViewById(R.id.movie_poster);
            mProgress = (ProgressBar) itemView.findViewById(R.id.movie_progress);

            mLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.layout_movie:
                    Intent intent = new Intent(context, MovieDetailsActivity.class);
                    intent.putExtra(context.getString(R.string.movie_id), movieResultTopRatedMovies.get(getAdapterPosition()).getId());
                    context.startActivity(intent);
                    break;
            }
        }
    }


    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        public LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = (ImageButton) itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = (TextView) itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = (LinearLayout) itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

}
