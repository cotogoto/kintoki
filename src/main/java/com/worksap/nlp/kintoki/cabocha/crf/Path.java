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

package com.worksap.nlp.kintoki.cabocha.crf;

import java.util.List;

public class Path {

    public Node rnode;
    public Node lnode;
    public List<Integer> fvector;
    public double cost;

    public Path() {
        clear();
    }

    public void clear() {
        rnode = null;
        lnode = null;
        fvector = null;
        cost = 0.0;
    }

    public void add(Node _lnode, Node _rnode) {
        lnode = _lnode;
        rnode = _rnode;
        lnode.rpath.add(this);
        rnode.lpath.add(this);
    }
}