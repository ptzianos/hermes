import os
from setuptools import setup, find_packages

with open(os.path.join(os.path.dirname(__file__), 'README.md')) as f:
    readme = f.read()

setup(
    name='hermes',
    version='0.0.1',
    description='Hermes Marketplace for IoT data',
    long_description=readme,
    long_description_content_type='text/markdown',
    author='Pavlos Tzianos',
    author_email='pavlos.tzianos@tum.de',
    url='',
    package_dir={'': 'src'},
    packages=find_packages(where='src', exclude=('tests', 'docs')),
    classifiers=(
        'Intended Audience :: Developers',
        'Natural Language :: English',
        'Programming Language :: Python',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.4',
        'Programming Language :: Python :: 3.5',
        'Programming Language :: Python :: 3.6',
        'Programming Language :: Python :: 3.7',
        'Programming Language :: Python :: Implementation :: CPython',
    ),
)