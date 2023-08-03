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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Chunk {

    private int           link;

    private int           headPos;

    private int           funcPos;

    private int           tokenPos;

    private List <Token>  tokens      = new ArrayList <>();

    private double        score;

    private List <String> featureList = new ArrayList <>();

    public int getTokenSize() {

        return this.tokens.size();
    }


    public boolean isEmpty() {

        return this.tokens.isEmpty();
    }


    public Token token(final int index) {

        return this.tokens.get(index);
    }


    public int getFeatureListSize() {

        return this.featureList.size();
    }


    public String getSurface() {

        return this.tokens.stream().map(Token::getSurface).collect(Collectors.joining());
    }
}
