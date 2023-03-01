# 実行前に

このファイルが存在するフォルダに
local.properties
を作成し、内容を
sdk.dir=C:\Users\yourname\AppData\Local\Android\Sdk
にする

.idea\workspace.xmlがなかったらバグる

以上の2つのファイルは.gitignoreから外しました(追跡対象となる)。

エラーが英語のとき
メニュー＞Help＞Edit Custom VM Options...を選ぶ
-Dfile.encoding=UTF-8
を追加する