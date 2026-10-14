#!/usr/bin/env python3
"""Publish only the rendered policy files to the existing GitHub Pages branch."""
from pathlib import Path
import os
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
BRANCH = 'codex/privacy-policy-pages'
REMOTE_REF = 'refs/remotes/origin/' + BRANCH
FILES = ('index.html', 'privacy-policy.html', '.nojekyll')


def git(*args, env=None, input=None):
    return subprocess.check_output(
        ['git', *args], cwd=ROOT, env=env, input=input, text=True
    ).strip()


def main():
    subprocess.run([sys.executable, str(ROOT / 'scripts/render_privacy_policy.py')], check=True)
    git('fetch', 'origin', 'refs/heads/' + BRANCH + ':' + REMOTE_REF)
    parent = git('rev-parse', REMOTE_REF)
    with tempfile.TemporaryDirectory(prefix='bokotan-policy-index-') as temporary:
        env = dict(os.environ, GIT_INDEX_FILE=str(Path(temporary) / 'index'))
        git('read-tree', '--empty', env=env)
        for name in FILES:
            blob = git('hash-object', '-w', str(ROOT / 'site/privacy-policy' / name))
            git('update-index', '--add', '--cacheinfo', '100644,' + blob + ',' + name, env=env)
        tree = git('write-tree', env=env)
        if tree == git('rev-parse', parent + '^{tree}'):
            print('Published policy already matches the generated files.')
            return
        commit = git('commit-tree', tree, '-p', parent, input='Update public privacy policy\n')
    # A normal fast-forward push refuses concurrent updates; never force push.
    git('push', 'origin', commit + ':refs/heads/' + BRANCH)
    print('Published policy update. Check the GitHub Pages deployment before submitting its URL.')


if __name__ == '__main__':
    main()
