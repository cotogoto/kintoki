package com.worksap.nlp.kintoki.cabocha;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TreeTest {

    @Test
    public void readAndWrite_shouldPreserveAdditionalInfoColumn() {
        Tree tree = new Tree();
        tree.read("東京\t名詞,固有名詞\tB-LOCATION\n", InputLayerType.INPUT_POS);
        tree.setOutputLayer(OutputLayerType.OUTPUT_POS);

        String actual = tree.toString(FormatType.FORMAT_LATTICE);

        assertTrue(actual.contains("東京\t名詞,固有名詞\tB-LOCATION"));
    }
}
