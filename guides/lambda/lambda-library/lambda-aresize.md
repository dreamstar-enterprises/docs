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

## ARESIZE

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aresize.1178644/){:target="_blank"}

### About

Resizes an array to a specified dimension.

#### Inputs:

  - a - array
  - [r] - new row number
  - [c] - new column number

#### More Info:

- if r/c omitted, array not resized, only errors/blanks replaced with null strings.
- if only one of the r/c arguments is omitted, the function calculates the smallest other argument to accommodate all elements of initial array.


### Code

{% capture code %}
ARESIZE = LAMBDA(a, [r], [c],
    LET(
        x, ROWS(a),
        y, COLUMNS(a),
        t, x * y,
        z, ROUNDUP(IF(r, r, IF(c, t / c, x)), 0),
        w, ROUNDUP(IF(c, c, IF(r, t / r, y)), 0),
        s, SEQUENCE(z, w),
        q, QUOTIENT(s - 1, y) + 1,
        m, MOD(s - 1, y) + 1,
        IFERROR(INDEX(IF(a = "", "", a), q, m), "")
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}