#!/bin/sh

$IN < /dev/stdin
gunzip $IN |

# Filter lines < 5
awk '{if (NF > 5) print $0;}' |

# Filter rubbish
sed '/[^[:alnum:][:space:]\.,:;\?!)(\$]/d' |
sed -n 's/[[:alpha:]]/&/p' |

# Strip spaces
sed "s|^\s*||" |
sed "s|\s*$||" |

# Sort sentences and deduplicate
sort | uniq |

# Tokenize
#perl tokenizer.perl |

# Print
less


