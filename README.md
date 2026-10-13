# Bokotan

Androidで動く英単語学習アプリです。

家から大学まで通うときに電車の中で英語音源を流して英単語を覚えたいという思いで開発しました。

単語データは単語帳を購入して、手打ち入力しています。音声データは出版社のサイトからダウンロードすることができます。

英語の音声はダウンロードできますが、日本語の音声はない場合があります。その時は、VOICEBOXを利用して作成しました。

# 注意

出版社由来の音声・単語データは含めていません。動作確認用のフリーな5語と英日音声はアプリに同梱しています。

従来の書籍の音声ファイルはスマホの内蔵ストレージのダウンロードフォルダ(/storage/emulated/0/Download/data)にあることを想定しています。

従来の書籍のデータファイルはapp/src/main/assetsにあることを想定しています。サンプルは追加操作なく使えます。

# アプリの特徴

* パス単、ユメタン、単熟語EXに対応しています。
* 単語の日本語訳、英訳を確認しながら発音を確認できます。
* テストモードでは、英単語が表示されるので日本語訳4択から一つ選んで答えます。
* 検索モードでは、全単語とその訳から一括検索できます。

![screen1](imgs/home.png)

起動時の画面


![screen2](imgs/playing.png)

再生画面


![screen3](imgs/quiz.png)

テストの画面


![screen4](imgs/search.png)

検索画面


# ビルドする方法

クローンしたフォルダをAndroid Studioで開きます(画像はWindows 10で開いた場合です)。

![img1](imgs/no%20config.png)

このままではビルドできないので、Add Configuration...をクリックします。このようなウィンドウが表示されます。

![img2](imgs/add%20config%20window.png)

左上の+をクリックし、Android Appを選択します。

![img3](imgs/select%20android%20app.png)

NameをAppにし、ModuleをBokotan.app.mainに設定します。

![img4](imgs/name%20and%20module.png)

OKを押してウィンドウを閉じます。これで、ビルドができるようになります。

# その他

## JDKのエラーが出たとき

メニュー>File>Settings>Build, Execution, Deployment>Buid Tools>Gradle

からJDKパスの設定画面を開く。Gradle JDKのパスを

C:\Program Files\Android\Android Studio\jbr

にする。

## エラーが英語のとき

メニュー＞Help＞Edit Custom VM Options...を選ぶ
-Dfile.encoding=UTF-8
を追加する


## バージョン1.03と同梱サンプル

欠損データ・クイズ・再生・画面再生成の不具合を修正しました。
更新内容と検証範囲は[更新記録](docs/release-1.03.txt)を参照してください。

5語と英日10音声はアプリに同梱しています。起動画面で「サンプル」を選ぶと、
追加のファイル配置やストレージ権限なしで再生・検索・クイズができます。
[サンプルの説明](samples/free-vocabulary/README.txt)も参照してください。ビルドはJDK 17で実行します。

```sh
bash gradlew assembleDebug
```

## GitHub ActionsのAPK・AAB

push、pull request、手動実行でテスト・Lint・ビルドを行います。
GitHubのActionsから実行結果を開くと、Artifactsから次をダウンロードできます。

- `bokotan-バージョン-debug-sample`：インストール可能なdebug APK、debug AAB、サンプル音声とライセンス。
- `bokotan-バージョン-release-unsigned`：同梱サンプル付きの未署名release APKとAAB。正式な配布には署名が必要です。
- `reports-実行ID-試行番号`：テスト・Lintのレポート。

debug/releaseをローカルで生成する場合:

```sh
bash gradlew testDebugUnitTest lintDebug assembleDebug bundleDebug assembleRelease bundleRelease
```

バージョンは[version.properties](version.properties)で管理します。
まとまった更新時に`VERSION_CODE`を増やし、`VERSION_NAME`を更新します。


## 署名付きGitHub Release

[署名設定と公開手順](docs/signed-release.txt)に記載した4つのRepository Secretsを登録します。
バージョンに一致する`v1.03`などのタグをpushすると、`Signed Release`が署名を検証し、
GitHub ReleasesにAPK・AAB・チェックサムを公開します。
