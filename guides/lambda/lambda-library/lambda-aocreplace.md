---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array 'By Element' functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## AOCREPLACE

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aocreplace.1165639/){:target="_blank"}

### About

Array occurrences replace, replaces only certain occurrences "oc" of a delimiter (any string) "d" in array "a", with a replacement delimiter (any string) "rd". 

#### Inputs:

   - a - array
   - d - any string or delimiter
   - rd - any string, replacement delimiter
   - oc - occurrence values to be replaced, 0 or ignored, repaces all occurrences, selective occurrences as constant integers row array {2,4,5} or single values, 2, 4. Values out of range are ignored.


### Code

{% capture code %}
AOCREPLACE = LAMBDA(a, d, rd, oc,
    LET(
        y, SORT(UNIQUE(oc, 1), , , 1),
        o, FILTER(y, y > 0, 0),
        n, COLUMNS(o),
        x, INDEX(o, 1, n),
        IF(
            n = 1,
            IF(
                o = 0,
                SUBSTITUTE(a, d, rd),
                SUBSTITUTE(a, d, rd, x)
            ),
            AOCREPLACE(
                SUBSTITUTE(a, d, rd, x),
                d,
                rd,
                INDEX(o, 1, SEQUENCE(, n - 1))
            )
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}