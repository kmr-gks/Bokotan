Bokotan デバッグ用サンプル（5語）

dog / 犬、cat / 猫、milk / 牛乳、fly / 蝿、flower / 花。
出版社の単語帳とは独立した、日本語・英語WordNetの同じsynsetから抽出した語。
一つの意味だけを選んだ最小サンプルであり、網羅的な辞書ではない。
音声はWikimedia Commonsの既存録音。LLM・有料API・合成音声は使用していない。

ファイル
・selection.json: 選んだ5語とWordNet synset、音声のCommonsファイル名。
・sources.json: 音声ごとの作者、出典URL、ライセンス、改変表示、SHA-256。
・licenses/: WordNetの英語・日本語データのライセンス原文。
・assets/dictionaries/Passtan/WordData1q.e.txt と .j.txt: 英日各5行。
・data/1q/: 英語5件（0000英.btv～0004英.btv）・日本語5件（0000訳.btv～0004訳.btv）。
  .btvはアプリが期待する拡張子。中身は原音のOggで、再エンコードしていない。
・assets/sample-notices/ と data/1q/sample-notices/: 出典とライセンスのコピー。

ビルド（リポジトリのルートから、JDK 17を選択）
  bash gradlew -PsampleData=true assembleDebug

APK: app/build/outputs/apk/debug/app-debug.apk
sampleData=trueを指定したdebugビルドだけに辞書サンプルを含める。
releaseには追加されない。通常のmain/assetsは変更していない。
独自の辞書をmain/assetsに持っている場合は、同名assetsとの重複を避けるため
このサンプルのビルドはデータを置いていない別のチェックアウトで実行する。

端末へ配置
現コードはDownload/data/1qをハードコードしているため、データのない
エミュレーターで試す。既存の1q音声へ上書きコピーしないこと。
Android SDKのplatform-toolsをPATHへ追加した状態で、リポジトリのルートから:
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  adb shell mkdir -p /storage/emulated/0/Download/data
  adb push samples/free-vocabulary/data/1q /storage/emulated/0/Download/data/

画面の選択
1. アプリのストレージ権限を許可する（現コードの動作上必要）。
2. 範囲は「パス単1級」を選択する。名前は既存の読み込み枠を借りているだけで、
   内容はその書籍の語順・単語・訳・音声を複製したものではない。
3. 空白処理は「オリジナル」、条件は「全て」を選ぶ。
4. 単語再生の開始位置は1を明示する。5件の有効範囲は0～4。
   現コードは通常1から再生し、周回も1へ戻るため、0番のdogはクイズまたは
   検索画面から確認する。これはサンプルで確認できる既存の番号設計の問題。
5. 「単語」または「クイズ」で試す。検索でも英日発音を確認できる。

制限
・他のプリセットのファイルは空。旧初期化処理の欠損ダイアログを避けるため。
  他の範囲、全範囲、文、単語+文、Unitによる番号変更はこのサンプルの対象外。
・ユメタン文の読込関数が空ファイルにも「null string」を自動追加するため、
  全文検索では5語とは別に4件のプレースホルダーが見えることがある。
・日本語の犬・猫の録音は、Commonsの説明によると非母語話者による録音。
  学習教材の品質保証ではなく、再生・表示・選択肢の動作を調べるサンプル。
・アプリの既存不具合は修正していない。点検結果はdocs/code-review-2026-10-13.txt。
・Android端末での実際の再生確認は未実施。Oggの形式・復号はホストで検証する。

利用条件
語彙: Japanese WordNet Copyright 2009 NICT、Princeton WordNet Copyright 2006。
ライセンス原文の著作権表示・免責条項を残して使用する。
音声: ファイルごとのCC BY-SA 3.0、CC BY、CC0または著作者のPublic domain表示。
音声の正確な条件はsources.jsonを参照。元録音を改変せず、ファイル名を変更した。
サンプルをコピー・配布するときはsources.jsonとlicenses/も一緒に含める。
このフォルダの外部データを、リポジトリ全体のApache-2.0で再ライセンスしない。

再取得（ネット接続時のみ）
  python3 scripts/fetch_sample_data.py
Python 3.8以上、追加パッケージ不要。保存済み音声は再利用する。
Commonsの頻度制限がある場合は時間をおいて再実行する。通常のビルドは通信不要。
