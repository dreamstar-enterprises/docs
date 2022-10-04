---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Descriptive Statistic & Basic Maths functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## ARUNTOT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aruntot.1167894/){:target="_blank"}

### About

Calculates running totals, on arrays, vertically by column, in 3 ways: all the way down; every k rows; every other k-th row.

#### Inputs:

  - ar - any array, nonnumeric values will be considered 0's
  - k - integer, 0 or ignored , running total all the way down, k > 0, every k rows, k < 0 (-k), every other k-th row

### Code

{% capture code %}
ARUNTOT = LAMBDA(ar, k,
    LET(
        n, ISNUMBER(ar),
        a, IF(n, ar, 0),
        r, ROWS(a),
        x, IF(k, MIN(INT(ABS(k)), r), r),
        c, COLUMNS(a),
        s, SEQUENCE(r),
        q, QUOTIENT(s - 1, x) + 1,
        m, MOD(s - 1, x) + 1,
        y, IF(s >= TRANSPOSE(s), IF(k >= 0, --(q = TRANSPOSE(q)), --(m = TRANSPOSE(m))), 0),
        MMULT(y, a)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}