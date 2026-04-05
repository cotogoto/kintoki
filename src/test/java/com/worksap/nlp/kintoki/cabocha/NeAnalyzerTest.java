package com.worksap.nlp.kintoki.cabocha;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NeAnalyzerTest {

    @Test
    public void getCharFeature_shouldClassifyCapitalizedWord() throws Exception {
        Method method = NeAnalyzer.class.getDeclaredMethod("getCharFeature", String.class);
        method.setAccessible(true);

        String feature = (String) method.invoke(null, "Tokyo");

        assertEquals("C", feature);
    }

    @Test
    public void concatFeature_shouldJoinUpToSpecifiedSize() throws Exception {
        Method method = NeAnalyzer.class.getDeclaredMethod("concatFeature", java.util.List.class, int.class);
        method.setAccessible(true);

        String feature = (String) method.invoke(null, Arrays.asList("名詞", "固有名詞", "一般", "*"), 4);

        assertEquals("名詞-固有名詞-一般", feature);
    }
}
