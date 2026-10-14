#!/usr/bin/env python3
"""Render the public policy as a standalone, script-free GitHub Pages site."""
from pathlib import Path
import html
import re

root = Path(__file__).resolve().parents[1]
folder = root / 'site/privacy-policy'
text = (folder / 'privacy-policy.txt').read_text(encoding='utf-8')
if any(value in text for value in ['PLACEHOLDER', '【未定】', '草案', 'メールアドレスを記入']):
    raise SystemExit('Policy contains unresolved placeholders; refusing to render.')
blocks = text.strip().split('\n\n')
date_match = re.search(r'^最終更新日: (\d{4}-\d{2}-\d{2})$', blocks[0], re.MULTILINE)
if date_match is None:
    raise SystemExit('Policy must specify its last updated date (YYYY-MM-DD).')
sections = []
for block in blocks[2:]:
    lines = block.splitlines()
    heading = html.escape(lines[0])
    body = '<br>'.join(html.escape(line) for line in lines[1:])
    # Only explicit HTTPS links from the policy source; no external scripts or fonts.
    body = re.sub(r'https://[^<\s]+', lambda m: '<a href="' + m.group() + '">' + m.group() + '</a>', body)
    sections.append('<section><h2>' + heading + '</h2><p>' + body + '</p></section>')
intro = '<br>'.join(html.escape(line) for line in blocks[1].splitlines())
intro = re.sub(r'https://[^<\s]+', lambda m: '<a href="' + m.group() + '">' + m.group() + '</a>', intro)
page = '''<!doctype html>
<html lang="ja"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Bokotan プライバシーポリシー</title>
<meta name="description" content="Bokotanのデータの利用、端末内保存、削除、お問い合わせについて。">
<link rel="canonical" href="https://kmr-gks.github.io/Bokotan/privacy-policy.html">
<style>
:root{color-scheme:light}*{box-sizing:border-box}body{margin:0;background:#fffaf2;color:#23352b;font:16px/1.85 system-ui,-apple-system,"Hiragino Sans",sans-serif}
main{max-width:850px;margin:auto;padding:48px 24px 72px}header{border-bottom:3px solid #ef9a35;padding-bottom:28px;margin-bottom:32px}
.brand{font-weight:700;font-size:18px;letter-spacing:.03em}h1{font-size:clamp(26px,5vw,36px);line-height:1.45;margin:12px 0}
.date{color:#536354;font-size:14px}.intro{background:#fff0d5;padding:20px 24px;border-radius:12px;margin-bottom:32px}
section{margin:30px 0}h2{font-size:20px;line-height:1.5;margin:0 0 10px}p{margin:0}a{color:#245f43;text-underline-offset:3px;overflow-wrap:anywhere}a:focus-visible{outline:3px solid #ef9a35;outline-offset:4px}
footer{border-top:1px solid #e4d8c4;margin-top:40px;padding-top:20px;font-size:14px;color:#536354}@media(max-width:480px){main{padding:28px 18px 48px}.intro{padding:16px}}
</style></head><body><main><header><div class="brand">Bokotan</div><h1>プライバシーポリシー</h1><div class="date">最終更新日: __UPDATED_DATE__</div></header>
<div class="intro">''' + intro + '</div>' + '\n'.join(sections) + '''<footer>Bokotan · 本ページはGitHub Pagesで公開しています。</footer></main></body></html>'''
page = page.replace('__UPDATED_DATE__', date_match.group(1))
for name in ['privacy-policy.html', 'index.html']:
    (folder / name).write_text(page, encoding='utf-8')
(folder / '.nojekyll').write_text('')
print('Rendered public policy HTML (no placeholders, no scripts).')
