package com.gk.iam.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private long total;
    private long pageNum;
    private long pageSize;
    private List<T> list;

    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return PageResult.<T>builder()
                .total(0)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .list(Collections.emptyList())
                .build();
    }

    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> list) {
        return PageResult.<T>builder()
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .list(list)
                .build();
    }
}

