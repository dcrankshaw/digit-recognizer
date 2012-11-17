#!/usr/bin/env python

import sys
import re
import string

def clean_line(line):
  words = line.split(' ')
  return map(clean_word, words)

def clean_word(word):
  # from stack overflow: http://stackoverflow.com/questions/1276764/stripping-everything-but-alphanumeric-chars-from-a-string-in-python
  pattern = re.compile('[\W_]+')
  return pattern.sub('', word).lower()

def main(source, dest):
  data_file = open(source, 'r')
  clean_data = open(dest, 'w')
  for line in data_file:
    val_word = clean_line(line)
    clean = ' '.join(clean_line(line))
    clean_data.write("%s\n" % clean)
    
    

if __name__ == '__main__':
  main(sys.argv[1], sys.argv[2])

 # main()
