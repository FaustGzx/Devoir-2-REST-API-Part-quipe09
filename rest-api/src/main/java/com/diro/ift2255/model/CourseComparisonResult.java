package com.diro.ift2255.model;

import java.util.List;

public class CourseComparisonResult {
    private List<CourseComparisonItem> items;

    public CourseComparisonResult(List<CourseComparisonItem> items) {
        this.items = items;
    }

    public List<CourseComparisonItem> getItems() { return items; }
}

