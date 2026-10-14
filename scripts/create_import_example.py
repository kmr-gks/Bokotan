#!/usr/bin/env python3
"""Build a public ZIP fixture from the licensed five-word sample. Never reads private files."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

root = Path(__file__).resolve().parents[1]
assets = root / "samples/free-vocabulary/assets"
output = root / "samples/free-vocabulary/import-example.zip"
english = (assets / "dictionaries/Sample/Words.e.txt").read_text().splitlines()
japanese = (assets / "dictionaries/Sample/Words.j.txt").read_text().splitlines()
rows = ["id\tenglish\tjapanese\tenglish_audio\tjapanese_audio"]
with ZipFile(output, "w", ZIP_DEFLATED) as archive:
    archive.writestr("package.properties", "format=1\nid=public-import-example\n")
    for i, (e, j) in enumerate(zip(english, japanese, strict=True)):
        en, ja = f"audio/{i:04d}-en.btv", f"audio/{i:04d}-ja.btv"
        rows.append(f"{i}\t{e}\t{j}\t{en}\t{ja}")
        archive.write(assets / f"sample-audio/{i:04d}-en.btv", en)
        archive.write(assets / f"sample-audio/{i:04d}-ja.btv", ja)
    # Notices travel with the public audio, but private packages need not include these.
    for file in (assets / "sample-notices").rglob("*"):
        if file.is_file() and file.name != ".DS_Store":
            archive.write(file, "audio/sample-notices/" + file.relative_to(assets / "sample-notices").as_posix())
    archive.writestr("words.tsv", "\n".join(rows) + "\n")
print(output)
