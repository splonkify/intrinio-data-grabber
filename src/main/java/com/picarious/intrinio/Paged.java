package com.picarious.intrinio;

public abstract class Paged {
    private int total_pages;
    private int current_page;

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public abstract <T extends Paged> void addPage(T pageEntity);
}
