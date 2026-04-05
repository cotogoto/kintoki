package com.worksap.nlp.kintoki.cabocha;

import java.io.IOException;
import java.util.List;

/**
 * Practical sample for integrating {@link Parser} in an application.
 *
 * <p>
 * This sample shows a pattern where the parser is initialized once at startup and reused for each
 * request.
 * </p>
 */
public class ParserUsageSample {

    public static class DependencyService {
        private final Parser parser;

        public DependencyService() throws IOException {
            this.parser = new Parser();
            this.parser.open();
        }

        public String parseSentence(String sentence) {
            return parser.parseToString(sentence);
        }

        public void parseBatch(List<String> sentences) {
            for (String sentence : sentences) {
                String parsed = parseSentence(sentence);
                System.out.println(parsed);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        DependencyService service = new DependencyService();
        String result = service.parseSentence("太郎は花子が読んでいる本を次郎に渡した。");
        System.out.println(result);
    }
}
