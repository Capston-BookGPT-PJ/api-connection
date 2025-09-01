package com.example.meltingbooks.browse;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.meltingbooks.Book;
import com.example.meltingbooks.R;

import java.util.List;


public class BrowseBookAdapter extends RecyclerView.Adapter<BrowseBookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;

    public BrowseBookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCoverImage;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookCoverImage = itemView.findViewById(R.id.bookCoverImage);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_book_item, parent, false);
        return new BookViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Glide.with(context).clear(holder.bookCoverImage);
        Book book = bookList.get(position);
        int widthDp = 300;
        int heightDp = 380;
        int cornerRadiusDp = 16; // 둥근 모서리 반경

        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, context.getResources().getDisplayMetrics());
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, context.getResources().getDisplayMetrics());
        int cornerRadiusPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadiusDp, context.getResources().getDisplayMetrics());

        RequestOptions requestOptions = new RequestOptions()
                .override(widthPx, heightPx)
                .transform(new FitCenter(), new RoundedCorners(cornerRadiusPx));


        Glide.with(context)
                .load(book.getImageUrl())
                .apply(requestOptions)
                .into(holder.bookCoverImage);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}