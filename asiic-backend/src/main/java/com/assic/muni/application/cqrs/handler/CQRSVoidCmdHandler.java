package com.assic.muni.application.cqrs.handler;

public interface CQRSVoidCmdHandler <T>{
    void handle(T cmd);
}
