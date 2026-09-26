package com.assic.muni.infrastructure.util;

public abstract  class ConstantsString {
    private static final String EMAIL_SUBJECT_BASE = "ASIIC Municipalidades - ";

    public static String getEmailSubjectBase(String extra){
        return EMAIL_SUBJECT_BASE + extra;
    }
}
