package koh.service.manager.vps.tools;

import java.util.Random;

public class RandomTools {
    static final int LENGTH = 16;
    static final String SPECIAL_CHARS = "@#$%^&[];~|";

    public static String randomPassword() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < LENGTH; ++i) {
            int tmp = random.nextInt();
            if (tmp % 5 == 0) {
                sb.append(SPECIAL_CHARS.charAt(Math.abs(random.nextInt() % SPECIAL_CHARS.length())));
            } else if (tmp % 4 == 0) {
                int c = Math.abs(random.nextInt() % 26) + 'A';
                sb.append((char) c);
            } else {
                int c = Math.abs(random.nextInt() % 26) + 'a';
                sb.append((char) c);
            }
        } return sb.toString();
    }
}
