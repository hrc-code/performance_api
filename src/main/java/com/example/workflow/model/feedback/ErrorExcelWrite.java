package com.example.workflow.model.feedback;



import java.util.List;



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
