package com.assic.muni.application.cqrs.handler;

public interface CQRSCmdHandler<T,R> {
    T handle(R cmd);
}
