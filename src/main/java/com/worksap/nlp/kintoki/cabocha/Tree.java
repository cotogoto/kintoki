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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.worksap.nlp.kintoki.cabocha.util.EastAsianWidth;
import com.worksap.nlp.sudachi.Morpheme;

public class Tree {

    private static final String EOS_NL   = "EOS\n";

    private OutputLayerType     outputLayer;

    private String              sentence = "";

    private List <Token>        tokens   = new ArrayList <>();

    private List <Chunk>        chunks   = new ArrayList <>();

    public void setSentence(final String sentence) {

        this.sentence = sentence;
    }


    public List <Token> getTokens() {

        return this.tokens;
    }


    public void setTokens(final List <Token> tokens) {

        this.tokens = tokens;
    }


    public List <Chunk> getChunks() {

        return this.chunks;
    }


    public void setChunks(final List <Chunk> chunks) {

        this.chunks = chunks;
    }


    public String getSentence() {

        if (this.isEmpty()) {
            return this.sentence;
        }
        return this.getTokens().stream().map(Token::getSurface).collect(Collectors.joining());
    }


    public int sentenceSize() {

        return this.sentence.length();
    }


    public Chunk chunk(final int index) {

        return this.chunks.get(index);
    }


    public Token token(final int index) {

        return this.tokens.get(index);
    }


    public void read(final String input, final InputLayerType inputLayer) {

        switch (inputLayer) {
            case INPUT_RAW_SENTENCE:
                this.sentence = input;
                break;
            case INPUT_POS:
            case INPUT_CHUNK:
            case INPUT_SELECTION:
            case INPUT_DEP:
                this.readCaboChaFormat(input, inputLayer);
                break;
            default:
                throw new IllegalArgumentException("Invalid input layer");
        }

        // verify chunk link
        if (this.getChunks().stream().map(Chunk::getLink).anyMatch(l -> l != -1 && (l >= this.getChunkSize() || l < -1))) {
            throw new IllegalArgumentException("Invalid dependencies");
        }
    }


    public void read(final List <Morpheme> morphemes) {

        for (final Morpheme m : morphemes) {
            final var token = new Token();
            token.setSurface(m.surface());
            token.setNormalizedSurface(m.normalizedForm());
            token.setPos(m.partOfSpeech().get(0));
            token.setFeature(String.join(",", m.partOfSpeech()));
            token.setFeatureList(m.partOfSpeech());
            token.setReading(m.readingForm());
            this.tokens.add(token);
        }
    }


    private void readCaboChaFormat(final String input, final InputLayerType inputLayer) {

        if (input.trim().isEmpty()) {
            return;
        }
        var chunkId = 0;
        for (final String line : input.split("\n")) {
            if (line.trim().isEmpty()) {
                throw new IllegalArgumentException("Invalid format");
            }
            if (line.startsWith("* ")) {
                if (inputLayer == InputLayerType.INPUT_POS) {
                    continue;
                }
                this.chunks.add(this.readHeader(line, chunkId));
                chunkId++;
            } else {
                final var token = this.readToken(line);
                this.getTokens().add(token);
                if (!this.chunks.isEmpty() && inputLayer.getValue() > Constant.CABOCHA_INPUT_POS) {
                    this.addTokenToLastChunk(token);
                }
            }
        }
        if (this.chunks.stream().anyMatch(c -> c.getTokenSize() == 0)) {
            throw new IllegalArgumentException("Empty chunk");
        }
    }


    private Chunk readHeader(final String line, final int chunkId) {

        final var columns = line.split(" ");
        if (columns.length < 3 || chunkId != Integer.parseInt(columns[1])) {
            throw new IllegalArgumentException("Invalid header format");
        }

        final var chunk = new Chunk();
        chunk.setLink(Integer.parseInt(columns[2].substring(0, columns[2].length() - 1)));

        if (columns.length >= 4) {
            final var value = Arrays.stream(columns[3].split("/")).mapToInt(Integer::valueOf).toArray();
            chunk.setHeadPos(value[0]);
            chunk.setFuncPos(value[1]);
        }

        if (columns.length >= 5) {
            chunk.setScore(Double.parseDouble(columns[4]));
        }

        if (columns.length >= 6) {
            chunk.setFeatureList(Arrays.asList(columns[5].split(",")));
        }

        return chunk;
    }


    private Token readToken(final String line) {

        final var columns = line.split("\t");
        if (columns.length < 2 || columns[0].isEmpty() || columns[1].isEmpty()) {
            throw new IllegalArgumentException("Invalid format");
        }

        final var token = new Token();
        token.setSurface(columns[0]);
        token.setNormalizedSurface(columns[0]);
        token.setFeature(columns[1]);
        token.setFeatureList(Arrays.asList(columns[1].split(",")));

        return token;
    }


    public String toString(final FormatType outputFormat) {

        final var sb = new StringBuilder();
        this.writeTree(sb, this.outputLayer, outputFormat);
        return sb.toString();
    }


    public void writeTree(final StringBuilder sb, final OutputLayerType outputLayer, final FormatType outputFormat) {

        switch (outputFormat) {
            case FORMAT_LATTICE:
                this.writeLattice(sb, outputLayer);
                break;
            case FORMAT_TREE_LATTICE:
                this.writeTree(sb);
                this.writeLattice(sb, outputLayer);
                break;
            case FORMAT_TREE:
                this.writeTree(sb);
                break;
            case FORMAT_XML:
            case FORMAT_CONLL:
                throw new UnsupportedOperationException("Not implemented");
            case FORMAT_NONE:
                break;
            default:
                throw new IllegalArgumentException("unknown format: " + outputFormat + "\n");
        }
    }


    private void writeLattice(final StringBuilder sb, final OutputLayerType outputLayer) {

        if (outputLayer == OutputLayerType.OUTPUT_RAW_SENTENCE) {
            sb.append(this.getSentence());
            sb.append("\n");
        } else {
            if (outputLayer == OutputLayerType.OUTPUT_POS) {
                sb.append(this.getTokens().stream().map(t -> t.getSurface() + "\t" + t.getFeature() + "\n")
                        .collect(Collectors.joining()));
            } else {
                var ci = 0;
                for (final Chunk chunk : this.getChunks()) {
                    this.writeChunk(sb, chunk, ci, outputLayer);
                    ci++;
                }
            }
            sb.append(Tree.EOS_NL);
        }
    }


    private void writeChunk(final StringBuilder sb, final Chunk chunk, final int id, final OutputLayerType outputLayer) {

        this.writeHeader1(sb, id, (outputLayer == OutputLayerType.OUTPUT_DEP) ? chunk.getLink() : -1);
        if (outputLayer != OutputLayerType.OUTPUT_CHUNK) {
            this.writeHeader2(sb, chunk);
            if (outputLayer == OutputLayerType.OUTPUT_SELECTION && chunk.getFeatureList() != null
                    && !chunk.getFeatureList().isEmpty()) {
                sb.append(' ').append(String.join(",", chunk.getFeatureList()));
            }
        }
        sb.append('\n');

        for (final Token token : chunk.getTokens()) {
            sb.append(token.getSurface()).append('\t').append(token.getFeature()).append('\n');
        }
    }


    private void writeHeader1(final StringBuilder sb, final int id, final int link) {

        sb.append("* ").append(id).append(' ').append(link).append("D");
    }


    private void writeHeader2(final StringBuilder sb, final Chunk chunk) {

        sb.append(' ').append(chunk.getHeadPos()).append('/').append(chunk.getFuncPos()).append(' ')
                .append(chunk.getScore());
    }


    private void writeTree(final StringBuilder sb) {

        final var size = this.getChunkSize();
        final Optional <Integer> maxLength = this.getChunks().stream()
                .map(chunk -> EastAsianWidth.getEastAsianWidth(chunk.getSurface()))
                .collect(Collectors.reducing(Integer::max));
        if (!maxLength.isPresent()) {
            sb.append(Tree.EOS_NL);
            return;
        }
        final int maxLen = maxLength.get();
        final var e = new boolean[size];

        for (var i = 0; i < size; ++i) {
            var isDep = false;
            final var link = this.chunk(i).getLink();
            final var surface = this.chunk(i).getSurface();
            final var rem = maxLen - EastAsianWidth.getEastAsianWidth(surface) + i * 2;
            for (var j = 0; j < rem; ++j) {
                sb.append(' ');
            }
            sb.append(surface);

            for (var j = i + 1; j < size; j++) {
                if (link == j) {
                    sb.append("-" + "D");
                    isDep = true;
                    e[j] = true;
                } else if (e[j]) {
                    sb.append(" |");
                } else if (isDep) {
                    sb.append("  ");
                } else {
                    sb.append("--");
                }
            }
            sb.append("\n");
        }

        sb.append(Tree.EOS_NL);
    }


    public boolean isEmpty() {

        return this.tokens.isEmpty();
    }


    public int getChunkSize() {

        return this.chunks.size();
    }


    public int getTokenSize() {

        return this.tokens.size();
    }


    public OutputLayerType getOutputLayer() {

        return this.outputLayer;
    }


    public void setOutputLayer(final OutputLayerType outputLayer) {

        this.outputLayer = outputLayer;
    }


    private void addTokenToLastChunk(final Token token) {

        this.chunks.get(this.chunks.size() - 1).getTokens().add(token);
    }
}
