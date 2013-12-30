#!/usr/bin/env python
import json
import re
import csv
import sys
import bz2file

FIELD_SEP = '\t'
CATEGORY_SEP = ','
NOT_WORD = re.compile(u'[^a-z0-9 ]+')
NO_LETTERS = re.compile(u'^[^a-z]+$')

labels = None
STOPWORDS = set()
URL_STOPWORDS = set()

with open('labels.json', 'r') as f:
    labels = json.load(f)

with open('stopwords.txt', 'r') as f:
    for line in f:
        STOPWORDS.add(line.rstrip())

with open('url_stopwords.txt', 'r') as f:
    for line in f:
        URL_STOPWORDS.add(line.rstrip())

line_no = 0
with bz2file.open(sys.argv[1], 'rb') as tsvfile:
    with open(sys.argv[2], 'w') as arfffile:
        # escape double quote
        csvwriter = csv.writer(arfffile, quoting=csv.QUOTE_ALL, doublequote=False, escapechar='\\')

        arfffile.write("@relation 40-cat-training\n")
        """
        arfffile.write("@attribute url string\n")
        arfffile.write("@attribute title string\n")
        """
        arfffile.write("@attribute tokens string\n")
        arfffile.write("@attribute klass {{{0}}}\n".format(",".join(labels)))
        arfffile.write("@data\n")

        for line in tsvfile:
            line_no += 1
            line = line.strip()
            try:
                url, title, keywords, categories = line.split(FIELD_SEP)
            except ValueError, e:
                print "ERROR at line {0}: {1} ".format(line_no, line)
                continue

            out_tokens = []

            def _tokenize(string, stop_words):
                for token in NOT_WORD.sub(" ", string.lower()).split():
                    match = NO_LETTERS.match(token)
                    if (token not in stop_words) and match is None and len(token) > 3:
                        out_tokens.append(token)

            _tokenize(url, URL_STOPWORDS)

            def _tokenize_zh(string):
                for token in string.split(' '):
                    token = token.strip()
                    if token not in STOPWORDS and len(token) > 3:
                        out_tokens.append(token)

            '''
            For chinese websites, we skip the segmentation which will be
            performed in the data2vector.sh.
            '''
            if len(sys.argv) > 3 and sys.argv[3] == 'zh-CN':
                _tokenize_zh(title)
                _tokenize_zh(keywords)
            else:
                _tokenize(title, STOPWORDS)
                _tokenize(keywords, STOPWORDS)

            # filter out all the escape character
            out_str = " ".join(out_tokens).replace('\\', '')

            if out_str == "":
                continue

            categories = categories.split(CATEGORY_SEP)
            for cat in categories:
                csvwriter.writerow([out_str, cat])
                #csvwriter.writerow([url, title, cat])
                #csvwriter.writerow([title, cat])
