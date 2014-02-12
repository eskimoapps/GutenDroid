package com.project.gutenberg.book;

import android.util.Log;
import com.project.gutenberg.book.parsing.Book_Parser;
import com.project.gutenberg.book.view.Book_View;
import com.project.gutenberg.util.Debug;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Chapter {
    private LinkedList<Page> pages;
    private LinkedList<String> paragraphs;
    private LinkedList<Integer[]> boundaries;
    private String title;
    private Book_Parser parser;
    private int chapter_index;
    public boolean first_page_loaded;
    public boolean last_page_loaded;
    private int first_loaded_page;
    private int last_loaded_page;

    private int list_relative_current_page_index = 0;

    private boolean loading_hook = false;
    private int loading_hook_stack_index = 0;
    private boolean loading_hook_next = false;
    private Book_View loading_hook_book_view;

    public Chapter(Book_Parser parser, int chapter_index) {
        pages = new LinkedList<Page>();
        boundaries = new LinkedList<Integer[]>();
        this.parser = parser;
        this.chapter_index = chapter_index;
    }

    public synchronized void set_title(String title) {
        this.title = title;
    }
    public synchronized String get_title() {
        return title;
    }
    public synchronized void set_paragraphs(LinkedList<String> paragraphs) {
        this.paragraphs = paragraphs;
    }
    public synchronized LinkedList<String> get_paragraphs() {
        if (paragraphs != null) {
           return paragraphs;
        } else {
            set_paragraphs(parser.parse_chapter(chapter_index));
            return paragraphs;
        }
    }
    public synchronized int number_of_pages() {
        return pages.size();
    }
    public synchronized Page get_page(int index) {
        try {
            return pages.get(index);
        } catch(IndexOutOfBoundsException e1) {

        } catch(NullPointerException e2) {

        } catch(NoSuchElementException e3) {

        }
        return null;
    }
    public synchronized Integer[] get_last_boundary() {
        try {
            return boundaries.getLast();
        } catch(IndexOutOfBoundsException e1) {

        } catch(NullPointerException e2) {

        } catch(NoSuchElementException e3) {

        }
        return null;
    }
    public synchronized Integer[] get_first_boundary() {
        try {
            return boundaries.getFirst();
        } catch(IndexOutOfBoundsException e1) {

        } catch(NullPointerException e2) {

        } catch(NoSuchElementException e3) {

        }
        return null;
    }
    public synchronized Integer[] get_current_page_boundaries() {
        return boundaries.get(list_relative_current_page_index);
    }
    public synchronized void add_page(boolean before, Page p){
        if (pages.size() == 0) {
            first_loaded_page = 0;
            last_loaded_page = 0;
            list_relative_current_page_index=0;
            pages.add(p);
        } else if (before) {
            pages.addFirst(p);
            first_loaded_page--;
            list_relative_current_page_index++;
        } else {
            pages.addLast(p);
            last_loaded_page++;
            if (loading_hook) {
                loading_hook_book_view.set_prev_current_next_page_lines(p.get_page_text(), loading_hook_stack_index);
            }
        }
    }
    public synchronized void add_boundary(boolean before, Integer[] b) {
        if (before) {
            boundaries.addFirst(b);
        } else {
            boundaries.addLast(b);
        }
    }
    public synchronized int get_list_relative_current_page_index() {
        return list_relative_current_page_index;
    }
    public synchronized Page next_page() {
        list_relative_current_page_index++;
        Debug.log("get next page: " + list_relative_current_page_index + "/" + pages.size());
        return pages.get(list_relative_current_page_index);
    }
    public synchronized Page previous_page() {
        list_relative_current_page_index--;
        return pages.get(list_relative_current_page_index);
    }
    public synchronized Page peek_next_page() {
        Debug.log("peek next page: " + (list_relative_current_page_index+1) + "/" + pages.size());
        return pages.get(list_relative_current_page_index+1);
    }
    public synchronized Page peek_current_page() {
        return pages.get(list_relative_current_page_index);
    }
    public synchronized Page peek_previous_page() {
        return pages.get(list_relative_current_page_index-1);
    }
    public synchronized Page peek_last_page() {
        return pages.get(pages.size()-1);
    }
    public synchronized boolean on_last_page() {
        return last_page_loaded && pages.size()-1 == list_relative_current_page_index;
    }
    public synchronized boolean on_penultimate_page() {
        return last_page_loaded && pages.size()-2 == list_relative_current_page_index;
    }
    public synchronized boolean on_first_page() {
        return list_relative_current_page_index == 0;
    }
    public synchronized boolean on_second_page() {
        return list_relative_current_page_index == 1;
    }
    public synchronized boolean set_last_page() {
        if (!last_page_loaded) return false;
        list_relative_current_page_index = pages.size()-1;
        return true;
    }
    public synchronized void set_first_page() {
        list_relative_current_page_index = 0;
    }
    public void set_containing_page(int paragraph, int word) {
        for (int i=0; i < boundaries.size(); i++) {
            Integer[] boundary = boundaries.get(i);
            if (boundary[4] > paragraph) {
                list_relative_current_page_index = i;return;
            }
            if (boundary[4] == paragraph) {
                if (boundary[5] > word || boundary[4] > paragraph) {
                    list_relative_current_page_index = i;return;
                }
            }
        }
        list_relative_current_page_index=0;
    }
}
