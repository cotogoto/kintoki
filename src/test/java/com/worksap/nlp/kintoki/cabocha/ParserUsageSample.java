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

        public Tree parseSentenceAsTree(String sentence) {
            return parser.parse(sentence);
        }

        public void parseBatch(List<String> sentences) {
            for (String sentence : sentences) {
                Tree tree = parseSentenceAsTree(sentence);
                dumpTree(tree, sentence);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Param param = buildParam();
        DependencyService service = new DependencyService(param);

        String sentence = "2026年4月1日午後3時に東京駅でソニー株式会社の山田太郎が1万円のチケットを3枚購入し、売上は前年比20%増加した。";
        Tree tree = service.parseSentenceAsTree(sentence);

        System.out.println("=== Sentence ===");
        System.out.println(sentence);
        System.out.println();

        dumpTree(tree, sentence);

        System.out.println();
        System.out.println("=== parseToString() ===");
        System.out.println(service.parseSentence(sentence));
    }

    private static Param buildParam() throws IOException {
        String baseDir = "src/test/resources";
        // String baseDir = "src/main/resources";

        String sudachiDictDir = baseDir + "/sudachi";
        String chunkerModel = baseDir + "/chunk.ipa.model";
        String parserModel = baseDir + "/dep.ipa.model";
        String neModel = baseDir + "/ne.ipa.model";

        validatePaths(sudachiDictDir, chunkerModel, parserModel, neModel);

        Param param = new Param();
        param.loadConfig();
        param.set(Param.SUDACHI_DICT, sudachiDictDir);
        param.set(Param.CHUNKER_MODEL, chunkerModel);
        param.set(Param.PARSER_MODEL, parserModel);
        param.set(Param.NE_MODEL, neModel);
        param.set(Param.NE, "1");
        param.set(Param.OUTPUT_LAYER, String.valueOf(Constant.CABOCHA_OUTPUT_DEP));

        return param;
    }

    private static void validatePaths(
            String sudachiDictDir,
            String chunkerModel,
            String parserModel,
            String neModel) {

        Path sudachiDirPath = Path.of(sudachiDictDir);

        if (!Files.isDirectory(sudachiDirPath)) {
            throw new IllegalArgumentException("Sudachi directory not found: " + sudachiDirPath.toAbsolutePath());
        }
        if (!Files.exists(sudachiDirPath.resolve("system_core.dic"))) {
            throw new IllegalArgumentException("system_core.dic not found in: " + sudachiDirPath.toAbsolutePath());
        }
        if (!Files.exists(sudachiDirPath.resolve("sudachi.json"))) {
            throw new IllegalArgumentException("sudachi.json not found in: " + sudachiDirPath.toAbsolutePath());
        }
        if (!Files.exists(Path.of(chunkerModel))) {
            throw new IllegalArgumentException("chunker model not found: " + Path.of(chunkerModel).toAbsolutePath());
        }
        if (!Files.exists(Path.of(parserModel))) {
            throw new IllegalArgumentException("parser model not found: " + Path.of(parserModel).toAbsolutePath());
        }
        if (!Files.exists(Path.of(neModel))) {
            throw new IllegalArgumentException("NE model not found: " + Path.of(neModel).toAbsolutePath());
        }
    }

    private static void dumpTree(Tree tree, String sentence) {
        System.out.println("=== Chunks / Tokens / NE ===");

        for (int i = 0; i < tree.getChunkSize(); i++) {
            Chunk chunk = tree.chunk(i);
            System.out.println("[Chunk " + i + "] link=" + chunk.getLink() + ", surface=" + chunk.getSurface());

            for (int j = 0; j < chunk.getTokenSize(); j++) {
                Token token = chunk.token(j);
                String surface = token.getSurface();
                String actualNe = normalizeNeLabel(token.getAdditionalInfo());

                System.out.println("  - surface      : " + surface);
                System.out.println("    normalized   : " + token.getNormalizedSurface());
                System.out.println("    pos/feature  : " + token.getFeature());
                System.out.println("    actual NE    : " + actualNe);
            }
        }

        System.out.println("EOS");
    }

    private static String normalizeNeLabel(String label) {
        if (label == null || label.isBlank()) {
            return "O";
        }
        return label;
    }
}
