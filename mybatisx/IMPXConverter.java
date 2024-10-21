package com.icc.framework.api.annotation.mybatisx;

public interface IMPXConverter<F,T> {
    T to(F orgin);
}
