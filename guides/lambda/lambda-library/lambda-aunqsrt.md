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

## AUNQSRT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aunqsrt.1164696/){:target="_blank"}

### About

Sorts ascending, descending, or extracts unique values by rows of an array, left aligned.

#### Inputs:

  - a - required array
  - k - -1 sort descending, 0 unique, 1 sort ascending

### Code

{% capture code %}
AUNQSRT = LAMBDA(ar, k,
    LET(
        xk, OR(k = {-1, 0, 1}),
        r, ROWS(ar),
        c, COLUMNS(ar),
        sr, SEQUENCE(r),
        s, SEQUENCE(r * c),
        q, QUOTIENT(s - 1, c) + 1,
        m, MOD(s - 1, c) + 1,
        a, INDEX(IF(ar = "", "", ar), q, m),
        x, a <> "",
        qf, FILTER(CHOOSE({1, 2}, q, a), x),
        y, SWITCH(k, -1, SORT(qf, {1, 2}, {1, -1}), 0, UNIQUE(qf), 1, SORT(qf, {1, 2})),
        na, INDEX(y, , 2),
        nq, INDEX(y, , 1),
        fq, FREQUENCY(nq, sr),
        p, INDEX(fq, sr),
        nc, MAX(p),
        nsa, IF(p >= SEQUENCE(, nc), SEQUENCE(r, nc)),
        nsr, SMALL(nsa, SEQUENCE(SUM(p))),
        rs, IFNA(XLOOKUP(nsa, nsr, na), ""),
        IF(xk, rs, "check var -1 (desc), 0 (unique), 1 (asc)")
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}