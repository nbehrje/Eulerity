package com.example.eulerity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{
    private static ArrayList<Bitmap> localDataSet;
    private static ArrayList<String> urls;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(view.getContext(), EditActivity.class);
                    String url = urls.get(getAdapterPosition());
                    i.putExtra("url", url);
                    view.getContext().startActivity(i);
                }
            });
            imageView = (ImageView) view.findViewById(R.id.imageView);
        }

        public ImageView getImageView() {
            return imageView;
        }
    }

    public GalleryAdapter() {
        localDataSet = new ArrayList<>();
        urls = new ArrayList<>();
    }


    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.gallery_item, viewGroup, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder viewHolder, int position) {
        viewHolder.getImageView().setImageBitmap(localDataSet.get(position));

    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void addItem(Bitmap item, String url){
        localDataSet.add(item);
        urls.add(url);
        notifyDataSetChanged();
    }

    public void setImg(int idx, Bitmap item){
        localDataSet.set(idx, item);
        notifyItemChanged(idx);
    }
}
