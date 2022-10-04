---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array Transformation functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## ACLEAN

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aclean.1164895/){:target="_blank"}

### About

Cleans an array, replaces errors with null strings or removes the rows with errors or removes the rows with errors and blanks or null strings. 

Calls [AUNIQUE](../lambda-library/lambda-aunique.html).


#### Inputs:

  - a - array
  - k - 0 replaces errors with null strings, 1 removes only rows with errors, 2 removes rows with errors and blanks or null strings


### Code

{% capture code %}
ACLEAN = LAMBDA(a, k,
    LET(
        xk, OR(k = {0, 1, 2}),
        r, ROWS(a),
        sr, SEQUENCE(r),
        x, ISERROR(a) * sr,
        y, sr * IFERROR(x + (a = ""), 1),
        z, AUNIQUE(SWITCH(k, 0, 0, 1, x, 2, y), ),
        xm, ISNA(XMATCH(sr, z)),
        IF(
            xk,
            IFERROR(FILTER(IF(a = "", "", a), xm), ""),
            "0 null strings for errors, 1 removes errors only, 2 removes errors and blanks"
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}