Bokotan 同梱サンプル（5語）

dog / 犬、cat / 猫、milk / 牛乳、fly / 蝿、flower / 花。
WordNetの同じsynsetから抽出した英日データと、Wikimedia Commonsの既存録音。
出版社のデータは使っていない。LLM・有料API・合成音声は使用していない。

使い方
APKをインストールして起動すると、5語が自動で読み込まれる。
起動画面の範囲で「サンプル」を選び、「単語」または「クイズ」を押す。
新規インストール時はサンプルが選択される。全文検索でも5語を検索・発音できる。
音声のダウンロード、ファイルのコピー、ストレージ権限の許可は不要。
例文は収録していない。条件を変更した場合、出題対象がなくなる場合がある。
開始位置は空欄で先頭から再生し、明示する場合は0～4。
「総合」にもサンプルを含めるが、成績は出版社の書籍とは別に保存する。

ファイル
assets/dictionaries/Sample/Words.e.txt / Words.j.txt: 英日各5行。
assets/sample-audio/: 英語5件（0000-en.btv～0004-en.btv）と日本語5件（0000-ja.btv～0004-ja.btv）。
音声は原音のOggをそのまま使用し、APKでは圧縮せず格納する。
assets/sample-notices/: 出典とライセンス。debug/releaseのAPK/AABすべてに同梱する。
sources.json: 作者、出典、ライセンス、元ファイルと同梱先のパス、SHA-1/SHA-256。
selection.json: 選んだ5語、WordNet synset、Commonsの音声ファイル名。
licenses/: 英語・日本語WordNetのライセンス原文。
data/1q/: 取得時の従来形式のコピー。アプリへ手動配置する必要はない。

ビルド（JDK 17、リポジトリのルートから）
  bash gradlew assembleDebug
特別なGradleオプションは不要。ネット接続は依存関係の取得時だけ必要。

利用条件
Japanese WordNet Copyright 2009 NICT、Princeton WordNet Copyright 2006。
著作権表示と免責条項を残して使用する。
音声の条件はファイルごとのCC BY-SA 3.0、CC0、著作者のPublic domain表示。
正確な作者と条件はsources.jsonを参照。録音は改変せず、同梱時のファイル名を変更した。
このフォルダのデータをリポジトリのApache-2.0で再ライセンスしない。
犬・猫の日本語録音はCommonsの説明によると非母語話者によるもの。

再取得（通常の使用・ビルドでは実行不要）
  python3 scripts/fetch_sample_data.py
Python 3.8以上、追加パッケージ不要。保存済み音声をハッシュ照合して再利用する。
