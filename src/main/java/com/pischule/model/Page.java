package com.pischule.model;

import java.util.List;

public record Page<T>(
        List<T> elements,
        int pageIndex,
        int pageSize,
        int totalElementsCount
) {

    public int elementsCount() {
        return elements.size();
    }

    public int previousPageIndex() {
        return pageIndex - 1;
    }

    public int nextPageIndex() {
        return pageIndex + 1;
    }

    public int startElementNumber() {
        return pageIndex * pageSize + 1;
    }

    public int endElementNumber() {
        return pageIndex * pageSize + elements.size();
    }

    public boolean hasPreviousPage() {
        return pageIndex > 0;
    }

    public boolean hasNextPage() {
        return pageIndex * pageSize + elements.size() < totalElementsCount;
    }
}
