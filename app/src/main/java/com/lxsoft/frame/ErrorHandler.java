package com.lxsoft.frame;

import android.content.Context;

import androidx.annotation.NonNull;

public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(@NonNull final Thread thread, @NonNull final Throwable ex) {
        LogWriter.LogToFile("崩溃信息："+ ex.getMessage());
        LogWriter.LogToFile("崩溃线程名称："+ thread.getName()+ "  线程ID："+ thread.getId());

        final StackTraceElement[] trace = ex.getStackTrace();
        for(final StackTraceElement element:trace){
            LogWriter.debugError("Lines: " + element.getLineNumber() + " : " + element.getMethodName());
        }
        ex.printStackTrace();
        FrameApplication.exitApp();

    }

    private static ErrorHandler instance;

    public static ErrorHandler getInstance() {
        if(ErrorHandler.instance == null){
            ErrorHandler.instance = new ErrorHandler();
        }
        return ErrorHandler.instance;
    }

    public static void setInstance(ErrorHandler instance) {
        ErrorHandler.instance = instance;
    }

    public ErrorHandler() {
    }

    public void setErrorHandler(final Context ctx){
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
}
