package com.rizky.ra.cmp.api;

import com.rizky.ra.cmp.validation.CheckStringContent;

public class ExampleRequest {
    
    @CheckStringContent(message="must contain John")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
}
