package edu.sharif.ce.gallivanter.core;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by mohammad on 11/10/16.
 */
public class MathUtils {
    public static double calculateJaccard(final String s1, final String s2) {
        Map<String, Integer> profile1 = getProfile(s1);
        Map<String, Integer> profile2 = getProfile(s2);

        Set<String> union = new HashSet<String>();
        union.addAll(profile1.keySet());
        union.addAll(profile2.keySet());

        int inter = 0;

        for (String key : union) {
            if (profile1.containsKey(key) && profile2.containsKey(key)) {
                inter++;
            }
        }
        double toReturn=1.0 * inter / union.size();
        return toReturn;
    }
    private static Map<String, Integer> getProfile(final String string) {
        HashMap<String, Integer> shingles = new HashMap<>();

        String string_no_space = SPACE_REG.matcher(string).replaceAll(" ");
        for (int i = 0; i < (string_no_space.length() - 2 + 1); i++) {
            String shingle = string_no_space.substring(i, i + 2);

            if (shingles.containsKey(shingle)) {
                shingles.put(shingle, shingles.get(shingle) + 1);

            } else {
                shingles.put(shingle, 1);

            }
        }

        return Collections.unmodifiableMap(shingles);
    }
    private static final Pattern SPACE_REG = Pattern.compile("\\s+");
    public static int minDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();

        // len1+1, len2+1, because finally return dp[len1][len2]
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);

                //if last two chars equal
                if (c1 == c2) {
                    //update dp value for +1 length
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }

        return dp[len1][len2];
    }

    public static double calculateLogTfForDoc(int numberOFTimesWhichTermOccursInDoc){
        if(!(numberOFTimesWhichTermOccursInDoc>0))
            return 0;
        return 1+Math.log(numberOFTimesWhichTermOccursInDoc);
    }

    public static double calculateIdfForTerm(int N,int numberOfDocsWhichContainTerm){
        return Math.log(N/numberOfDocsWhichContainTerm);
    }
}
