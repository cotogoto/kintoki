/*
 * Copyright 2019 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.kintoki.cabocha;

import com.worksap.nlp.kintoki.cabocha.crf.Tagger;

import java.io.IOException;
import java.util.List;

public class NeAnalyzer implements Analyzer {

    private static final double CRF_COST_FACTOR = 1.0;
    private static final String DEFAULT_NE = "O";

    private Tagger tagger;

    @Override
    public void open(Param param) throws IOException {
        String path = param.getString(Param.NE_MODEL);
        tagger = Tagger.openBinaryModel(path, CRF_COST_FACTOR);
    }

    @Override
    public void parse(Tree tree) {
        int tokenSize = tree.getTokenSize();
        for (int i = 0; i < tokenSize; i++) {
            Token token = tree.token(i);
            tagger.add(token.getNormalizedSurface(), getCharFeature(token.getNormalizedSurface()),
                    concatFeature(token.getFeatureList(), 4));
        }

        tagger.parse();
        List<String> labels = tagger.ynames();
        for (int i = 0; i < tokenSize; i++) {
            String label = labels.get(tagger.y(i));
            tree.token(i).setAdditionalInfo(label);
        }
        tagger.clear();
    }

    private static String concatFeature(List<String> featureList, int size) {
        StringBuilder output = new StringBuilder();
        int minSize = Math.min(featureList.size(), size);
        for (int i = 0; i < minSize; ++i) {
            if (("*").equals(featureList.get(i))) {
                break;
            }
            if (i != 0) {
                output.append("-");
            }
            output.append(featureList.get(i));
        }
        return output.toString();
    }

    private static String getCharFeature(String surface) {
        if (surface == null || surface.isEmpty()) {
            return DEFAULT_NE;
        }
        int type1 = -1;
        int type2 = -1;
        boolean isCapitalizedWord = true;
        int codePointCount = 0;

        for (int offset = 0; offset < surface.length();) {
            int codePoint = surface.codePointAt(offset);
            int c = mapCharClass(codePoint);

            if (isCapitalizedWord && c != CharClass.ALPHA.id) {
                isCapitalizedWord = false;
            }
            if (codePointCount == 0) {
                type1 = c;
            } else if (codePointCount == 1 && type1 == CharClass.CAPALPHA.id && c == CharClass.ALPHA.id) {
                type2 = type1;
                type1 = c;
                isCapitalizedWord = true;
            } else if (c == CharClass.HYPHEN.id) {
                // skip hyphen
            } else if (c != type1) {
                type2 = type1;
                type1 = c;
            }
            codePointCount++;
            offset += Character.charCount(codePoint);
        }

        if (isCapitalizedWord) {
            return "C";
        }
        if (codePointCount == 1) {
            return "S" + type1;
        }
        if (type2 == -1) {
            return "M" + type1;
        }
        return "T" + type2 + "-" + type1;
    }

    private static int mapCharClass(int cp) {
        if (cp == '-') {
            return CharClass.HYPHEN.id;
        }
        if (cp >= 'A' && cp <= 'Z') {
            return CharClass.CAPALPHA.id;
        }
        if ((cp >= 'a' && cp <= 'z') || Character.UnicodeScript.of(cp) == Character.UnicodeScript.LATIN) {
            return CharClass.ALPHA.id;
        }
        if (Character.isDigit(cp)) {
            return CharClass.NUMBER.id;
        }
        if (Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN) {
            return CharClass.KANJI.id;
        }
        if (Character.UnicodeScript.of(cp) == Character.UnicodeScript.HIRAGANA) {
            return CharClass.HIRAGANA.id;
        }
        if (Character.UnicodeScript.of(cp) == Character.UnicodeScript.KATAKANA) {
            return CharClass.KATAKANA.id;
        }
        return CharClass.OTHER.id;
    }

    private enum CharClass {
        OTHER(0), NUMBER(1), KANJI(2), HIRAGANA(3), KATAKANA(4), ALPHA(5), CAPALPHA(6), HYPHEN(7);

        final int id;

        CharClass(int id) {
            this.id = id;
        }
    }
}
