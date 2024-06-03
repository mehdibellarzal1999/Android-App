package com.traveler.traveljournal;

import android.content.Context;
import android.util.Patterns;

import java.util.regex.Pattern;

public class Validation {

    private Context context;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
//                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{5,}" +               //at least 4 characters
                    "$");

    private boolean isValidPasswordForm(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public ValidationResponse validateUsername(String username) {
        if (username.isEmpty()) {
            return new ValidationResponse(false, context.getString(R.string.please_enter_your_username));
        } else if (username.contains(" ")) {
            return new ValidationResponse(false, context.getString(R.string.username_must_not_contain_white_space));
        } else if (username.length() < 5) {
            return new ValidationResponse(false, context.getString(R.string.username_must_be_5_chars_at_least));
        } else if (username.length() > 16) {
            return new ValidationResponse(false, context.getString(R.string.Username_must_beless_than_16_chars));
        } else return new ValidationResponse(true, null);
    }

    public ValidationResponse validateEmail(String email) {
        if (email.isEmpty()) {
            return new ValidationResponse(false, context.getString(R.string.please_enter_your_email));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResponse(false, context.getString(R.string.please_enter_valid_email));
        } else {
            return new ValidationResponse(true, null);
        }
    }


    public ValidationResponse validatePassword(String password) {
        if (password.isEmpty()) {
            return new ValidationResponse(false, context.getString(R.string.please_enter_your_password));
        } else if (password.contains(" ")) {
            return new ValidationResponse(false, context.getString(R.string.password_must_not_contain_whitespaces));
        } else if (!isValidPasswordForm(password)) {
            return new ValidationResponse(false, context.getString(R.string.password_must_be_at_least_5_chars_and_1_digit));
        } else {
            return new ValidationResponse(true, null);
        }
    }
}
