package com.worksap.nlp.kintoki.cabocha;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParamTest {

    @Test
    public void loadConfig_shouldIncludeNeSettings() throws Exception {
        Param param = new Param();
        param.loadConfig();

        assertEquals("ne.ipa.model", param.getString(Param.NE_MODEL));
        assertEquals(0, param.getInt(Param.NE));
    }
}
