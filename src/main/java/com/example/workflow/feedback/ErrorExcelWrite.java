package com.example.workflow.feedback;


import org.apache.poi.ss.formula.functions.T;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;


public class ErrorExcelWrite {

    private static final ThreadLocal<List<?>> errorCollection = new ThreadLocal<>();

    public static void setErrorCollection(List<?> errorEmployeeLists) {
        errorCollection.set(errorEmployeeLists);
    }

    public static List<?> getErrorCollection() {
        return errorCollection.get();
    }

    public static void clearErrorCollection() {
        errorCollection.remove();
    }
}
