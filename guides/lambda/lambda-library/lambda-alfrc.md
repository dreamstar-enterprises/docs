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

## ALFCR

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/alfcr.1168202/){:target="_blank"}

### About

Array Last or First values, by Columns or Rows, blanks excluded.

#### Inputs:

   - ar - array
   - lf - last or first arg. (0 or 1). 0 for last, 1 for first
   - cr - clms rows arg. (0 or 1). 0 for clms, 1 for rows

### Code

{% capture code %}
ALFCR = LAMBDA(ar, lf, cr,
    LET(
        z, {0, 1},
        x, IF(lf, 1, -1),
        r, ROWS(ar),
        c, COLUMNS(ar),
        s, SEQUENCE(r * c),
        y, IF(cr, SEQUENCE(, c), SEQUENCE(r)),
        q, QUOTIENT(s - 1, c),
        m, MOD(s - 1, c),
        a, INDEX(IF(ar = "", "", ar), q + 1, m + 1),
        xm, XMATCH(y, 1 / (a <> "") + IF(cr, m, q), , x),
        IF(
            AND(OR(lf = z), OR(cr = z)),
            IFNA(INDEX(a, xm), ""),
            "only 0 or 1 values"
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}