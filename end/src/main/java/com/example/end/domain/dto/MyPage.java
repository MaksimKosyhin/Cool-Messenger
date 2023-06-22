package com.example.end.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MyPage(
        @Min(value = 1, message = "Paging must start with page 1") long number,
        @Min(value = 1, message = "You can request minimum 1 records")
        @Max(value = 100, message = "You can request maximum 100 records") long limit) {

    public MyPage() {
        this(1, 100);
    }
    public MyPage(int number) {this(number, 100);}
}
