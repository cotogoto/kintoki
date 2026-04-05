# Kintoki（日本語ドキュメント）

[![](https://jitpack.io/v/cotogoto/kintoki.svg)](https://jitpack.io/#cotogoto/kintoki)

Kintoki は日本語係り受け解析ライブラリです。

このライブラリには次の機能が含まれます。

- CaboCha の Java 移植
- コマンドラインツール
- 文節解析（Chunking）および係り受け解析（Parsing）用モデル

## フォーク運用に関するお知らせ

このリポジトリはフォークとして運用しています。
本フォークでは、継続的なメンテナンスと必要に応じた改造・改善を行っています。

## 動作要件

- Java 17 以上

## インストール

以下の `VERSION` は、JitPack に表示される最新バージョンに置き換えてください。

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependency>
    <groupId>com.github.cotogoto</groupId>
    <artifactId>kintoki</artifactId>
    <version>VERSION</version>
</dependency>
```

## 設定

本ライブラリは形態素解析に Sudachi を利用し、文節解析モデルと係り受け解析モデルを別々に読み込みます。  
利用前に以下を設定してください。

- Sudachi 辞書ディレクトリ
- 文節解析モデル
- 係り受け解析モデル

設定方法は `cabocharc.properties` を使うのが基本です。

```properties
# Parser model file name
parser-model  = dep.bccwj.model

# Chunker model file name
chunker-model = chunk.bccwj.model

# Named entity model file name（任意）
ne-model = ne.ipa.model

# Sudachi
sudachi-dict = ./

# 固有表現モード
# 0 - 無効（デフォルト）
# 1 - NE 付与を有効化
# 2 - NE 付与を有効化（kintoki では 1 と同等）
ne = 0
```

デフォルトでは resources 配下の `cabocharc.properties` が使われます。  
`Cabocha` や `Parser` のコンストラクタで設定ファイルのパスを指定することもできます。

```java
String path = "cabocharc.properties";
Cabocha cabocha = new Cabocha(path);
Tree tree = cabocha.parse(sent);
```

## API の使い分け

- `Cabocha` クラス: CLI 利用向け API
- `Parser` クラス: ライブラリ利用向け API

必要なインターフェースを外部から呼び出して利用できます。

## 主要 API（抜粋）

### `Cabocha`

```java
public Cabocha() throws IOException {...}
public Cabocha(String config) throws IOException {...}
public Cabocha(Param param) throws IOException {...}

public Tree parse(String sent) throws IOException {...}
public Tree parse(Tree sent) throws IOException {...}
public String parseToString(String sent) throws IOException {...}

public static void parse(String[] args) throws IOException {...}
```

### `Parser`

```java
public Parser() throws IOException {...}
public Parser(String config) throws IOException {...}
public Parser(Param param) throws IOException {...}

public void open() throws IOException {...}

public Tree parse(Tree tree) {...}
public Tree parse(String text) {...}
public String parseToString(String sent) {...}
```

## 実行例

### `Cabocha` を使う例

```java
public class Example {
    public static void main(String[] args) throws IOException {
        String sent = "太郎は花子が読んでいる本を次郎に渡した。";
        Cabocha cabocha = new Cabocha();
        Tree tree = cabocha.parse(sent);
        System.out.println(tree.toString(FormatType.FORMAT_TREE));
    }
}
```

### `Parser` を使う例

```java
public class Example {
    public static void main(String[] args) throws IOException {
        String configPath = "cabocharc.properties";
        Parser parser = new Parser(configPath);
        parser.open();
        Tree tree = parser.parse("太郎は花子が読んでいる本を次郎に渡した。");
        System.out.println(tree.toString(FormatType.FORMAT_TREE));
    }
}
```

## 実運用を想定した利用サンプル

以下は「アプリ起動時に `Parser` を 1 度だけ初期化し、リクエストごとに `parseToString` を呼ぶ」構成のサンプルです。  
Web API やバッチ処理などでそのまま使える最小構成になっています。

実際のサンプルクラスは `src/test/java/com/worksap/nlp/kintoki/cabocha/ParserUsageSample.java` に追加しています。

```java
import com.worksap.nlp.kintoki.cabocha.Parser;

import java.io.IOException;
import java.util.List;

public class DependencyService {
    private final Parser parser;

    public DependencyService(String configPath) throws IOException {
        this.parser = new Parser(configPath);
        this.parser.open(); // 起動時に 1 回だけ
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
```

```java
public class App {
    public static void main(String[] args) throws IOException {
        DependencyService service = new DependencyService("cabocharc.properties");
        String result = service.parseSentence("太郎は花子が読んでいる本を次郎に渡した。");
        System.out.println(result);
    }
}
```

出力は以下のような CaboCha 互換フォーマットになります。

```text
* 0 5D 0/1 ...
太郎    名詞,固有名詞,人名,名,*,*
は      助詞,係助詞,*,*,*,*
...
EOS
```
