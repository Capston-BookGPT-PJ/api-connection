package com.example.meltingbooks.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.Book;
import com.example.meltingbooks.R;

import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;

    public SearchBookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookThumbnail;
        TextView bookInfoTitle, bookInfoAuthor, bookInfoPublisher;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookThumbnail = itemView.findViewById(R.id.bookThumbnail);
            bookInfoTitle = itemView.findViewById(R.id.bookInfoTitle);
            bookInfoAuthor = itemView.findViewById(R.id.bookInfoAuthor);
            bookInfoPublisher = itemView.findViewById(R.id.bookInfoPublisher);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_book_list, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.bookInfoTitle.setText(book.getTitle());
        holder.bookInfoAuthor.setText(book.getAuthor());
        holder.bookInfoPublisher.setText(book.getPublisher());

        Glide.with(context)
                .load(book.getImageUrl())
                .into(holder.bookThumbnail);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}

