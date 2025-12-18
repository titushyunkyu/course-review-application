package edu.virginia.sde.reviews;

public class ValidationUtils {
    public static boolean isValidSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return false;
        }
        return subject.trim().matches("[A-Za-z]{2,4}");
    }

    public static boolean isValidNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            return false;
        }
        return number.trim().matches("\\d{4}");
    }

    public static boolean isValidTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        String trimmed = title.trim();
        return trimmed.length() >= 1 && trimmed.length() <= 50;
    }

    public static String capitalizeSubject(String subject) {
        return subject == null ? null : subject.trim().toUpperCase();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static String getSubjectErrorMessage() {
        return "Subject must be 2-4 letters only (no numbers or symbols)";
    }

    public static String getNumberErrorMessage() {
        return "Course number must be exactly 4 digits";
    }

    public static String getTitleErrorMessage() {
        return "Title must be between 1 and 50 characters";
    }
}
