#!/usr/bin/env python3
"""Download five licensed vocabulary/recording pairs; no LLM or paid API.

Only writes samples/free-vocabulary. Run once with network access; builds are offline.
"""
import hashlib
import html
import json
from pathlib import Path
import re
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / 'samples/free-vocabulary'
COMMONS = 'https://commons.wikimedia.org/w/api.php'
OMW = 'https://raw.githubusercontent.com/omwn/omw-data/main/wns/'
ALLOWED_AUDIO_LICENSES = {'cc0', 'cc-by-3.0-us', 'cc-by-3.0', 'cc-by-4.0',
                          'cc-by-sa-3.0', 'cc-by-sa-4.0', 'pd'}


def fetch(url):
    request = urllib.request.Request(url, headers={
        'User-Agent': 'Bokotan-debug-fixtures/1.0 (local vocabulary sample)'
    })
    for attempt in range(4):
        time.sleep(1.5)
        try:
            with urllib.request.urlopen(request, timeout=45) as response:
                return response.read()
        except urllib.error.HTTPError as error:
            if error.code not in (429, 502, 503, 522) or attempt == 3:
                raise
            time.sleep(max(15, min(60, int(error.headers.get('Retry-After', '30')))))


def commons_info(**query):
    params = dict(action='query', prop='imageinfo', iiprop='url|extmetadata|sha1',
                  format='json', redirects=1, **query)
    result = json.loads(fetch(COMMONS + '?' + urllib.parse.urlencode(params)))
    page = next(iter(result['query']['pages'].values()))
    info = page['imageinfo'][0]
    meta = info['extmetadata']
    license_id = meta['License']['value']
    if license_id == 'pd' and 'PD-self' not in meta.get('Categories', {}).get('value', ''):
        raise ValueError('Only author-dedicated public domain recordings are accepted')
    if license_id not in ALLOWED_AUDIO_LICENSES:
        raise ValueError(f"Unapproved license: {page['title']}: {license_id}")
    return page, info


def text(value):
    return html.unescape(re.sub('<[^>]+>', '', value))


def recording(info, filename):
    meta = info['extmetadata']
    url = info['url'].split('?')[0]
    target = DEST / filename
    data = target.read_bytes() if target.exists() else fetch(url)
    if hashlib.sha1(data).hexdigest() != info['sha1']:
        data = fetch(url)
    if hashlib.sha1(data).hexdigest() != info['sha1']:
        raise ValueError(f'Recording differs from Commons source: {url}')
    if not data.startswith(b'OggS'):
        raise ValueError(f'Expected an Ogg recording: {url}')
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
    return dict(file=filename, source_url=info['descriptionurl'], download_url=url,
                author=text(meta.get('Artist', {}).get('value', '')),
                title=text(meta.get('ObjectName', {}).get('value', '')),
                description=text(meta.get('ImageDescription', {}).get('value', '')),
                credit=text(meta.get('Credit', {}).get('value', '')),
                license=meta['LicenseShortName']['value'],
                license_url=meta.get('LicenseUrl', {}).get('value', info['descriptionurl'] + '#Licensing'),
                modifications='Unmodified recording; renamed to .btv for Bokotan.',
                source_sha1=info['sha1'],
                sha256=hashlib.sha256(data).hexdigest())


def main():
    rows = json.loads((DEST / 'selection.json').read_text())
    if len(rows) != 5:
        raise ValueError('Expected exactly five sample words')
    notices = DEST / 'licenses'
    notices.mkdir(exist_ok=True)
    dictionaries = {}
    datasets = []
    for lang in ('eng', 'jpn'):
        url = OMW + f'{lang}/wn-data-{lang}.tab'
        raw = fetch(url)
        dictionaries[lang] = raw.decode('utf-8').splitlines()
        (notices / f'wordnet-{lang}-LICENSE.txt').write_bytes(fetch(OMW + lang + '/LICENSE'))
        datasets.append(dict(language=lang, source_url=url,
                             sha256=hashlib.sha256(raw).hexdigest()))
    for row in rows:
        for lang, field in [('eng', 'english'), ('jpn', 'japanese')]:
            if not any(parts[0] == row['synset'] and 'lemma' in parts[1]
                       and parts[2] == row[field]
                       for line in dictionaries[lang]
                       if len(parts := line.split('\t')) == 3):
                raise ValueError(f'WordNet alignment not found: {row}')
    audio = []
    for index, row in enumerate(rows):
        page, info = commons_info(titles=row['english_audio_title'])
        audio.append(recording(info, f'data/1q/{index:04d}英.btv'))
        page, info = commons_info(titles=row['japanese_audio_title'])
        audio.append(recording(info, f'data/1q/{index:04d}訳.btv'))
        print(f"{index}: {row['english']} / {row['japanese']}", flush=True)
    assets = DEST / 'assets'
    def write(name, lines):
        path = assets / 'dictionaries' / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(''.join(line + '\n' for line in lines), encoding='utf-8')
    for lang, field in [('e', 'english'), ('j', 'japanese')]:
        write(f'Passtan/WordData1q.{lang}.txt', [row[field] for row in rows])
    manifest = dict(vocabulary=rows, datasets=datasets, audio=audio)
    (DEST / 'sources.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n')
    # Ship credits together with the sample APK and the external recordings.
    for destination in (assets / 'sample-notices', DEST / 'data/1q/sample-notices'):
        destination.mkdir(parents=True, exist_ok=True)
        for source in [DEST / 'sources.json', *notices.glob('*.txt')]:
            (destination / source.name).write_bytes(source.read_bytes())
    print('Created five word pairs and ten original recordings.')


if __name__ == '__main__':
    main()
