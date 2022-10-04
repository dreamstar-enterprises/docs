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

## ASPLIT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/asplit.1164750/){:target="_blank"}

### About

Splits a column array by a delimiter.

Calls [AUNQSRT](../lambda-library/lambda-aunqsrt.html).

#### Inputs:

   - ar - required. Column array
   - d - required. String or number delimiter

### Code

{% capture code %}
ASPLIT = LAMBDA(ar, d,
    LET(
        a, TRIM(ar),
        s, SEQUENCE(, MAX(LEN(a))),
        x, IFERROR(SEARCH(d, d & a, s), ""),
        y, IFERROR(SEARCH(d, a & d, s), ""),
        m, AUNQSRT(x, ),
        n, AUNQSRT(y, ),
        z, IFERROR(TRIM(MID(a, m, n - m)), ""),
        IF(ISERROR(--z), z, --z)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}