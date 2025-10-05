package com.example.meltingbooks.network.log;

public class ReadingLogResponse {
    private int id;
    private int userId;
    private int bookId;
    private int pagesRead;
    private int minutesRead;
    private String readAt; // "2025-09-23T00:00:00"

    public int getMinutesRead() { return minutesRead; }
    public String getReadAt() { return readAt; }
    public int getPagesRead(){return pagesRead;}
    public int getBookId(){return bookId;}
    public int getId(){return id;}
}
