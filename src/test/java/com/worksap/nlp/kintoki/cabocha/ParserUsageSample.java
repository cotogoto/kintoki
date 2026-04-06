package com.worksap.nlp.kintoki.cabocha;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        public DependencyService(Param param) throws IOException {
            this.parser = new Parser(param);
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
        Param param = buildParam(args);
        if (param == null) {
            return;
        }
        DependencyService service = new DependencyService(param);
        String result = service.parseSentence("太郎は花子が読んでいる本を次郎に渡した。");
        System.out.println(result);
    }

    private static Param buildParam(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: ParserUsageSample <sudachi-dict-dir> <chunker-model> <parser-model>");
            System.out.println("Example: ParserUsageSample ./dict ./chunk.ipa.model ./dep.ipa.model");
            System.out.println("Skip execution because required files are not provided.");
            return null;
        }

        String sudachiDictDir = args[0];
        String chunkerModel = args[1];
        String parserModel = args[2];

        if (!Files.exists(Path.of(sudachiDictDir, "system_core.dic"))) {
            throw new IllegalArgumentException("system_core.dic not found in: " + sudachiDictDir);
        }
        if (!Files.exists(Path.of(chunkerModel))) {
            throw new IllegalArgumentException("chunker model not found: " + chunkerModel);
        }
        if (!Files.exists(Path.of(parserModel))) {
            throw new IllegalArgumentException("parser model not found: " + parserModel);
        }

        Param param = new Param();
        param.loadConfig();
        param.set(Param.SUDACHI_DICT, sudachiDictDir);
        param.set(Param.CHUNKER_MODEL, chunkerModel);
        param.set(Param.PARSER_MODEL, parserModel);
        return param;
    }
}
