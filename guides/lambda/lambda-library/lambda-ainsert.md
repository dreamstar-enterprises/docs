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

## AINSERT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/ainsert.1165643/){:target="_blank"}

### About

Inserts any string delimiter "d" between characters of an array "a" with a selectable pace "p" and selectable starting point "i".

#### Inputs:

   - a - array
   - d - any string delimiter
   - p - integer, pace width
   - i - integer, index starting point, first delimiter will be placed in the i th position, or just after (i-1)th character, so for i ignored or i<=2, insert will start after 1st character.

### Code

{% capture code %}
AINSERT = LAMBDA(a, d, p, i,
    LET(
        ld, LEN(d),
        k, MAX(p, 1),
        j, MAX(k + 1, i),
        n, MAX(LEN(a)),
        IF(
            n < (j - 1),
            SUBSTITUTE(TRIM(SUBSTITUTE(a, d, " ")), " ", d),
            AINSERT(
                REPLACE(a, j, , d),
                d,
                k,
                j + 2 * (k + 1) - k - 1 + ld - 1
            )
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}