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

## AUNSTACK

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aunstack.1180037/){:target="_blank"}

### About

Unstacks an array for a certain height [h].

### Code

{% capture code %}
AUNSTACK = LAMBDA(a, [h],
    LET(
        x, ROWS(a),
        y, COLUMNS(a),
        k, MEDIAN(1, ABS(h), x),
        n, ROUNDUP(x / k, 0),
        IFERROR(
            MAKEARRAY(
                k,
                n * y,
                LAMBDA(r, c, INDEX(IF(a = "", "", a), k * QUOTIENT(c - 1, y) + r, MOD(c - 1, y) + 1))
            ),
            ""
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}