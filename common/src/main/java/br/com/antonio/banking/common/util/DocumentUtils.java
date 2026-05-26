package br.com.antonio.banking.common.util;

/**
 * Utility for CPF/CNPJ validation and formatting.
 * Pure static methods — no Spring dependency.
 */
public final class DocumentUtils {

    private DocumentUtils() {}

    public static boolean isValidCpf(String cpf) {
        if (cpf == null) return false;
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) return false;
        return checkDigit(digits, 10) && checkDigit(digits, 11);
    }

    public static boolean isValidCnpj(String cnpj) {
        if (cnpj == null) return false;
        String digits = cnpj.replaceAll("\\D", "");
        if (digits.length() != 14 || digits.chars().distinct().count() == 1) return false;
        return checkCnpjDigit(digits, 12) && checkCnpjDigit(digits, 13);
    }

    public static String sanitize(String document) {
        return document == null ? null : document.replaceAll("\\D", "");
    }

    private static boolean checkDigit(String cpf, int position) {
        int sum = 0;
        for (int i = 0; i < position - 1; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (position - i);
        }
        int remainder = (sum * 10) % 11;
        int digit = (remainder == 10 || remainder == 11) ? 0 : remainder;
        return digit == Character.getNumericValue(cpf.charAt(position - 1));
    }

    private static boolean checkCnpjDigit(String cnpj, int position) {
        int[] weights = position == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        int digit = remainder < 2 ? 0 : 11 - remainder;
        return digit == Character.getNumericValue(cnpj.charAt(position));
    }
}