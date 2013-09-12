#!/bin/sh

$IN < /dev/stdin
cat $IN |

# Filter lines < 5
awk '{if (NF > 5) print $0;}' |

# Strip spaces
sed "s|^\s*||" |
sed "s|\s*$||" |

# Join sentences
#perl -p -e 's/\n/metros /'

# Sort sentences
sort | uniq -c | sort -nr |


# Tokenize
#perl tokenizer.perl |

# Print
less


