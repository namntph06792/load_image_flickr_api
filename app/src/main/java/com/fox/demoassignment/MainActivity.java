package com.fox.demoassignment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.fox.demoassignment.adapter.StaggeredRecycleViewAdapter;
import com.fox.demoassignment.listener.EndlessRecyclerViewScrollListener;
import com.fox.demoassignment.model.FavouritePhoto;
import com.fox.demoassignment.model.Photo;


import java.util.ArrayList;
import java.util.List;

import static com.fox.demoassignment.constants.Constant.API_KEY;
import static com.fox.demoassignment.constants.Constant.USER_ID;

public class MainActivity extends AppCompatActivity {

    //Init component
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    //Adapter
    private StaggeredRecycleViewAdapter adapter;
    //
    private List<Photo> photoList = new ArrayList<>();
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initRecycleView();
        fetchPhotoFromApi(page);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.this.page = 1;
                photoList.clear();
                fetchPhotoFromApi(MainActivity.this.page);
                onLoadMore();
            }
        });

        onLoadMore();

    }

    private void onLoadMore(){
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                MainActivity.this.page++;
                fetchPhotoFromApi(MainActivity.this.page++);
            }
        });
    }

    private void fetchPhotoFromApi(int page){
        AndroidNetworking.post("https://www.flickr.com/services/rest/")
                .addBodyParameter("method", "flickr.favorites.getList")
                .addBodyParameter("api_key", API_KEY)
                .addBodyParameter("user_id", USER_ID)
                .addBodyParameter("format", "json")
                .addBodyParameter("extras", "views, media, path_alias, url_sq, url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o")
                .addBodyParameter("nojsoncallback", "1")
                .addBodyParameter("per_page", "10")
                .addBodyParameter("page", String.valueOf(page))
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsObject(FavouritePhoto.class, new ParsedRequestListener<Object>() {

                    @Override
                    public void onResponse(Object response) {
                        swipeLayout.setRefreshing(false);
                        FavouritePhoto favouritePhoto = (FavouritePhoto) response;
                        List<Photo> photos = favouritePhoto.getPhotos().getPhoto();
                        MainActivity.this.photoList.addAll(photos);
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onError(ANError anError) {
                        // handle error
                        Toast.makeText(MainActivity.this, anError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews(){
        recyclerView = findViewById(R.id.recycleView);
        swipeLayout = findViewById(R.id.swipeLayout);
        adapter = new StaggeredRecycleViewAdapter(this,photoList);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
    }

    private void initRecycleView(){
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(adapter);
    }


}
