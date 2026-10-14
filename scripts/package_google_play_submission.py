#!/usr/bin/env python3
"""Bundle reviewed submission drafts and the signed AAB; never reads personal vocabulary."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib

root = Path(__file__).resolve().parents[1]
work = root / 'build/google-play'
output = work / 'Bokotan-1.05-play-submission-draft.zip'
aab = work / 'Bokotan-1.05.aab'
expected = '55de187feb120eabfe08d9c92c960f28937e1bf2683cd4d101f4b18f1c77f914'
if not aab.is_file() or hashlib.sha256(aab.read_bytes()).hexdigest() != expected:
    raise SystemExit('Download and verify the signed v1.05 AAB first. See docs/google-play/artifact-info.txt.')
with ZipFile(output, 'w', ZIP_DEFLATED) as archive:
    archive.write(aab, 'Bokotan-1.05.aab')
    for file in sorted((root / 'docs/google-play').rglob('*')):
        if file.is_file() and file.suffix in {'.txt', '.html', '.png', '.svg'}:
            archive.write(file, 'submission/' + file.relative_to(root / 'docs/google-play').as_posix())
    archive.write(root / 'samples/free-vocabulary/import-example.zip', 'public-import-example.zip')
    for name in ['media-playback-demo.mp4', 'zip-import-demo.mp4']:
        file = work / name
        if file.is_file(): archive.write(file, 'review-videos/' + name)
print(output)
print('SHA-256: ' + hashlib.sha256(output.read_bytes()).hexdigest())
