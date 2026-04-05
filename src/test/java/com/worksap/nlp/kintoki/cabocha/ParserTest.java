package com.worksap.nlp.kintoki.cabocha;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class ParserTest {

    @Test
    public void constructor_shouldLoadNeModeFromParam() throws Exception {
        Param param = new Param();
        param.set(Param.INPUT_LAYER, Constant.CABOCHA_INPUT_RAW_SENTENCE);
        param.set(Param.OUTPUT_LAYER, Constant.CABOCHA_OUTPUT_RAW_SENTENCE);
        param.set(Param.OUTPUT_FORMAT, Constant.CABOCHA_FORMAT_LATTICE);
        param.set(Param.NE, 2);

        Parser parser = new Parser(param);
        Field neField = Parser.class.getDeclaredField("ne");
        neField.setAccessible(true);

        assertEquals(2, neField.getInt(parser));
    }
}
