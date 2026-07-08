package com.skyline.app.network;

import java.io.Serializable;
import java.util.List;

public class Blog implements Serializable {
    public String blogCode;
    public String title;
    public String slug;
    public String category;
    public String categorySlug;
    public String destination;
    public String destinationCode;
    public String shortDescription;
    public String introContent;
    public String thumbnailUrl;
    public String coverImageUrl;
    public Author author;
    public String publishedDate;
    public String readTime;
    public int likesCount;
    public int viewsCount;
    public List<QuickInfo> quickInfos;
    public List<Section> sections;
    public CTA cta;
    public boolean isFeatured;

    public static class Author implements Serializable {
        public String authorId;
        public String name;
        public String avatarUrl;
    }

    public static class QuickInfo implements Serializable {
        public String quickInfoId;
        public String icon;
        public String title;
        public String value;
    }

    public static class Section implements Serializable {
        public String sectionId;
        public int sectionNumber;
        public String title;
        public String type;
        public List<SectionItem> items;
    }

    public static class SectionItem implements Serializable {
        public String itemId;
        public String title;
        public String subtitle;
        public String description;
        public String imageUrl;
        public String icon;
        public List<String> bulletPoints;
    }

    public static class CTA implements Serializable {
        public String text;
        public String action;
        public String destinationCode;
    }
}
