package com.assic.muni.application.cqrs.handler;

public interface CQRSHandler <T,R> {
    T handle(R cmd);
}
